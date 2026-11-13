using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using OficioCR.Api.Configuration;
using OficioCR.Api.Data;
using OficioCR.Api.Endpoints;
using OficioCR.Api.Models;
using OficioCR.Api.Security;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("OficioCR") ??
        Environment.GetEnvironmentVariable("DATABASE_URL") ??
        "Host=localhost;Port=5432;Database=oficiocr;Username=oficiocr;Password=oficiocr_local"));
builder.Services.AddDistributedMemoryCache();
builder.Services.AddHttpContextAccessor();
builder.Services.AddScoped<CurrentUser>();
builder.Services.AddScoped<AccountWorkflowService>();
builder.Services.AddScoped<EmailSender>();
builder.Services.AddScoped<IPasswordHasher<User>, PasswordHasher<User>>();
builder.Services.AddOficioCors(builder.Configuration);
builder.Services.AddOficioCookieAuthentication(builder.Environment);

var app = builder.Build();
app.UseCors();
app.UseAuthentication();
app.Use(async (context, next) =>
{
    var method = context.Request.Method;
    var isUnsafeMethod = method is "POST" or "PUT" or "PATCH" or "DELETE";
    if (isUnsafeMethod && context.Request.Path.StartsWithSegments("/api"))
    {
        var antiforgery = context.RequestServices.GetRequiredService<Microsoft.AspNetCore.Antiforgery.IAntiforgery>();
        try { await antiforgery.ValidateRequestAsync(context); }
        catch (Microsoft.AspNetCore.Antiforgery.AntiforgeryValidationException)
        {
            context.Response.StatusCode = StatusCodes.Status400BadRequest;
            await context.Response.WriteAsJsonAsync(new { message = "La solicitud de seguridad venció. Recarga la página e inténtalo de nuevo." });
            return;
        }
    }
    await next();
});
app.UseAuthorization();

await DatabaseInitializer.InitializeAsync(app.Services, app.Configuration);

app.MapGet("/api/health", () => Results.Ok(new { status = "ok" }));
app.MapGet("/api/auth/csrf", (HttpContext context, Microsoft.AspNetCore.Antiforgery.IAntiforgery antiforgery) =>
{
    var tokens = antiforgery.GetAndStoreTokens(context);
    return Results.Ok(new { requestToken = tokens.RequestToken });
});
app.MapAuthEndpoints();
app.MapAdminEndpoints();
app.MapJobEndpoints();
if (builder.Configuration.GetValue<bool>("ENABLE_TEST_ENDPOINTS"))
    app.MapTestUserEndpoints();

app.Run();
