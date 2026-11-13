using Microsoft.AspNetCore.Authentication.Cookies;
using OficioCR.Api.Security;

namespace OficioCR.Api.Configuration;

public static class AuthenticationExtensions
{
    public static IServiceCollection AddOficioCookieAuthentication(this IServiceCollection services, IWebHostEnvironment environment)
    {
        services.AddSingleton<DistributedCacheTicketStore>();
        services.AddSingleton<ITicketStore>(provider =>
            provider.GetRequiredService<DistributedCacheTicketStore>());
        services.AddAuthentication(CookieAuthenticationDefaults.AuthenticationScheme)
            .AddCookie(options =>
            {
                ConfigureCookie(options.Cookie, environment, "oficiocr.session", SameSiteMode.Lax);
                options.ExpireTimeSpan = SessionPolicy.Lifetime;
                options.SlidingExpiration = false;
                options.Events.OnRedirectToLogin = context =>
                {
                    context.Response.StatusCode = StatusCodes.Status401Unauthorized;
                    return Task.CompletedTask;
                };
                options.Events.OnRedirectToAccessDenied = context =>
                {
                    context.Response.StatusCode = StatusCodes.Status403Forbidden;
                    return Task.CompletedTask;
                };
            });
        services.AddOptions<CookieAuthenticationOptions>(CookieAuthenticationDefaults.AuthenticationScheme)
            .Configure<ITicketStore>((options, ticketStore) => options.SessionStore = ticketStore);

        services.AddAntiforgery(options =>
        {
            options.HeaderName = "X-CSRF-TOKEN";
            ConfigureCookie(options.Cookie, environment, "oficiocr.csrf", SameSiteMode.Strict);
        });
        services.AddAuthorization();
        return services;
    }

    public static IServiceCollection AddOficioCors(this IServiceCollection services, IConfiguration configuration)
    {
        var origins = (configuration["FRONTEND_ORIGIN"] ?? "http://localhost:4200")
            .Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries);
        services.AddCors(options => options.AddDefaultPolicy(policy =>
            policy.WithOrigins(origins).AllowAnyHeader().AllowAnyMethod().AllowCredentials()));
        return services;
    }

    private static void ConfigureCookie(CookieBuilder cookie, IWebHostEnvironment environment, string name, SameSiteMode sameSite)
    {
        cookie.Name = environment.IsDevelopment() ? name : $"__Host-{name}";
        cookie.HttpOnly = true;
        cookie.SecurePolicy = environment.IsDevelopment() ? CookieSecurePolicy.SameAsRequest : CookieSecurePolicy.Always;
        cookie.SameSite = sameSite;
        cookie.Path = "/";
        cookie.IsEssential = true;
    }
}

