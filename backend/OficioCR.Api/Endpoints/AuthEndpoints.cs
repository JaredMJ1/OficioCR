using System.Security.Claims;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using OficioCR.Api.Contracts.Auth;
using OficioCR.Api.Contracts.Users;
using OficioCR.Api.Data;
using OficioCR.Api.Models;
using OficioCR.Api.Security;

namespace OficioCR.Api.Endpoints;

public static class AuthEndpoints
{
    public static IEndpointRouteBuilder MapAuthEndpoints(this IEndpointRouteBuilder endpoints)
    {
        var group = endpoints.MapGroup("/api/auth");
        group.MapPost("/register", RegisterAsync);
        group.MapPost("/login", LoginAsync);
        group.MapPost("/logout", (Delegate)(Func<HttpContext, Task<IResult>>)LogoutAsync).RequireAuthorization();
        group.MapPost("/password/forgot", ForgotPasswordAsync);
        group.MapPost("/password/reset", ResetPasswordAsync);
        group.MapGet("/me", GetCurrentUser).RequireAuthorization();
        group.MapPut("/me", UpdateCurrentUserAsync).RequireAuthorization();
        return endpoints;
    }

    internal static async Task<IResult> RegisterAsync(
        RegisterRequest request,
        AppDbContext database,
        IPasswordHasher<User> passwordHasher)
    {
        if (!UserInput.TryNormalize(request.Name, request.Email, out var name, out var email))
            return Results.BadRequest(new { message = "Nombre y correo válidos son obligatorios." });
        if (!PasswordPolicy.IsValid(request.Password))
            return Results.BadRequest(new { message = "La contraseña debe cumplir todos los requisitos indicados." });
        if (await database.Users.AnyAsync(user => user.Email == email))
            return Results.Conflict(new { message = "Ya existe una cuenta con ese correo." });

        var user = new User { Name = name, Email = email, Role = "usuario" };
        user.PasswordHash = passwordHasher.HashPassword(user, request.Password!);
        database.Users.Add(user);
        await database.SaveChangesAsync();
        return Results.Created("/api/auth/me", new { message = "Cuenta creada. Ya puedes iniciar sesión." });
    }

    private static async Task<IResult> LoginAsync(
        LoginRequest request,
        HttpContext context,
        AppDbContext database,
        IPasswordHasher<User> passwordHasher)
    {
        var email = request.Email?.Trim().ToLowerInvariant();
        var user = email is null ? null : await database.Users.SingleOrDefaultAsync(item => item.Email == email);
        if (user is null || passwordHasher.VerifyHashedPassword(user, user.PasswordHash, request.Password ?? "") == PasswordVerificationResult.Failed)
            return Results.Json(new { message = "Correo o contraseña incorrectos." }, statusCode: StatusCodes.Status401Unauthorized);
        var expiresAt = DateTimeOffset.UtcNow.Add(SessionPolicy.Lifetime);
        await context.SignInAsync(CookieAuthenticationDefaults.AuthenticationScheme, CreatePrincipal(user), CreateSessionProperties(expiresAt));
        return Results.Ok(new SessionResponse(expiresAt, new UserResponse(user.Id, user.Name, user.Email, user.Role)));
    }

    private static async Task<IResult> LogoutAsync(HttpContext context)
    {
        await context.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
        return Results.NoContent();
    }

    private static async Task<IResult> ForgotPasswordAsync(EmailRequest request, AppDbContext database, AccountWorkflowService workflow)
    {
        var email = request.Email?.Trim().ToLowerInvariant();
        var user = email is null ? null : await database.Users.SingleOrDefaultAsync(item => item.Email == email);
        if (user is not null)
        {
            try { await workflow.SendPasswordResetAsync(user.Email, user.Name); }
            catch (InvalidOperationException exception)
            {
                return Results.Problem(exception.Message, statusCode: StatusCodes.Status503ServiceUnavailable);
            }
        }
        return Results.Ok(new { message = "Si existe una cuenta con ese correo, recibirás instrucciones." });
    }

    private static async Task<IResult> ResetPasswordAsync(ResetPasswordRequest request, AppDbContext database,
        AccountWorkflowService workflow, IPasswordHasher<User> passwordHasher)
    {
        var email = request.Email?.Trim().ToLowerInvariant();
        if (email is null || !PasswordPolicy.IsValid(request.Password))
            return Results.BadRequest(new { message = "Correo o contraseña no válidos. La contraseña debe cumplir todos los requisitos." });
        var user = await database.Users.SingleOrDefaultAsync(item => item.Email == email);
        if (user is null || !await workflow.ConsumeAsync("reset", email, request.Token ?? ""))
            return Results.BadRequest(new { message = "El código no es válido o venció. Solicita otro." });
        user.PasswordHash = passwordHasher.HashPassword(user, request.Password!);
        await database.SaveChangesAsync();
        return Results.Ok(new { message = "Contraseña actualizada. Ya puedes iniciar sesión." });
    }

    private static async Task<IResult> UpdateCurrentUserAsync(UpdateProfileRequest request, CurrentUser currentUser,
        AppDbContext database, HttpContext context)
    {
        var sessionUser = currentUser.Get();
        if (sessionUser is null) return Results.Unauthorized();
        if (!UserInput.TryNormalize(request.Name, request.Email, out var name, out var email))
            return Results.BadRequest(new { message = "Nombre y correo válidos son obligatorios." });
        if (await database.Users.AnyAsync(user => user.Email == email && user.Id != sessionUser.Id))
            return Results.Conflict(new { message = "Ya existe una cuenta con ese correo." });
        var user = await database.Users.SingleOrDefaultAsync(user => user.Id == sessionUser.Id);
        if (user is null) return Results.Unauthorized();
        user.Name = name;
        user.Email = email;
        await database.SaveChangesAsync();
        await context.SignInAsync(CookieAuthenticationDefaults.AuthenticationScheme,
            CreatePrincipal(user), CreateSessionProperties(DateTimeOffset.UtcNow.Add(SessionPolicy.Lifetime)));
        return Results.Ok(new UserResponse(user.Id, user.Name, user.Email, user.Role));
    }

    private static IResult GetCurrentUser(CurrentUser currentUser)
    {
        var user = currentUser.Get();
        return user is null ? Results.Unauthorized() : Results.Ok(user);
    }

    private static ClaimsPrincipal CreatePrincipal(User user) => new(new ClaimsIdentity(
        [new(ClaimTypes.NameIdentifier, user.Id.ToString()), new(ClaimTypes.Name, user.Name),
         new(ClaimTypes.Email, user.Email), new(ClaimTypes.Role, user.Role)],
        CookieAuthenticationDefaults.AuthenticationScheme));

    private static AuthenticationProperties CreateSessionProperties(DateTimeOffset expiresAt) => new()
    {
        IsPersistent = false,
        AllowRefresh = false,
        ExpiresUtc = expiresAt
    };
}

public sealed record EmailRequest(string? Email);
public sealed record ResetPasswordRequest(string? Email, string? Token, string? Password);
public sealed record UpdateProfileRequest(string? Name, string? Email);
