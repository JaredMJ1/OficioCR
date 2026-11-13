using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using OficioCR.Api.Contracts.Auth;
using OficioCR.Api.Contracts.Users;
using OficioCR.Api.Data;
using OficioCR.Api.Models;
using OficioCR.Api.Security;

namespace OficioCR.Api.Endpoints;

public static class AdminEndpoints
{
    public static IEndpointRouteBuilder MapAdminEndpoints(this IEndpointRouteBuilder endpoints)
    {
        endpoints.MapGet("/api/users", GetUsersAsync)
            .RequireAuthorization(policy => policy.RequireRole("superusuario"));
        endpoints.MapPost("/api/admin/operators", CreateOperatorAsync)
            .RequireAuthorization(policy => policy.RequireRole("superusuario"));
        return endpoints;
    }

    public static IEndpointRouteBuilder MapTestUserEndpoints(this IEndpointRouteBuilder endpoints)
    {
        var group = endpoints.MapGroup("/api/test/users")
            .RequireAuthorization(policy => policy.RequireRole("superusuario"));
        group.MapGet("", GetUsersAsync);
        group.MapPost("", AuthEndpoints.RegisterAsync);
        group.MapDelete("/{id:guid}", DeleteUserAsync);
        return endpoints;
    }

    private static async Task<IResult> GetUsersAsync(AppDbContext database)
    {
        var users = await database.Users.OrderBy(user => user.CreatedAt)
            .Select(user => new AdminUserResponse(user.Id, user.Name, user.Email, user.Role, user.CreatedAt))
            .ToListAsync();
        return Results.Ok(users);
    }

    private static async Task<IResult> CreateOperatorAsync(RegisterRequest request, AppDbContext database,
        IPasswordHasher<User> passwordHasher)
    {
        if (!UserInput.TryNormalize(request.Name, request.Email, out var name, out var email))
            return Results.BadRequest(new { message = "Nombre y correo válidos son obligatorios." });
        if (!PasswordPolicy.IsValid(request.Password))
            return Results.BadRequest(new { message = "La contraseña no cumple los requisitos de seguridad." });
        if (await database.Users.AnyAsync(user => user.Email == email))
            return Results.Conflict(new { message = "Ya existe una cuenta con ese correo." });

        var user = new User { Name = name, Email = email, Role = "operador" };
        user.PasswordHash = passwordHasher.HashPassword(user, request.Password!);
        database.Users.Add(user);
        await database.SaveChangesAsync();
        return Results.Created("/api/users", new { message = "Operador creado. Ya puede iniciar sesión." });
    }

    private static async Task<IResult> DeleteUserAsync(Guid id, CurrentUser currentUser, AppDbContext database)
    {
        if (currentUser.Get()?.Id == id)
            return Results.Conflict(new { message = "No puedes eliminar la cuenta con la que iniciaste sesión." });

        var user = await database.Users.SingleOrDefaultAsync(item => item.Id == id);
        if (user is null) return Results.NotFound(new { message = "No se encontró el usuario." });
        if (user.Role == "superusuario" && await database.Users.CountAsync(item => item.Role == "superusuario") <= 1)
            return Results.Conflict(new { message = "Debe permanecer al menos un superusuario." });

        database.Users.Remove(user);
        await database.SaveChangesAsync();
        return Results.NoContent();
    }
}
