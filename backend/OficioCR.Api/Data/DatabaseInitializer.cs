using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using OficioCR.Api.Models;

namespace OficioCR.Api.Data;

public static class DatabaseInitializer
{
    public static async Task InitializeAsync(IServiceProvider services, IConfiguration configuration)
    {
        await using var scope = services.CreateAsyncScope();
        var database = scope.ServiceProvider.GetRequiredService<AppDbContext>();
        await database.Database.EnsureCreatedAsync();
        await database.Database.ExecuteSqlRawAsync("""
            CREATE TABLE IF NOT EXISTS "JobListings" (
                "Id" uuid NOT NULL PRIMARY KEY,
                "Title" character varying(120) NOT NULL,
                "Description" character varying(2000) NOT NULL,
                "Category" character varying(60) NOT NULL,
                "Location" character varying(100) NOT NULL,
                "AuthorId" uuid NOT NULL,
                "AuthorName" character varying(120) NOT NULL,
                "CreatedAt" timestamp with time zone NOT NULL
            );
            CREATE INDEX IF NOT EXISTS "IX_JobListings_CreatedAt" ON "JobListings" ("CreatedAt");
            ALTER TABLE "JobListings" ADD COLUMN IF NOT EXISTS "AcceptedById" uuid NULL;
            ALTER TABLE "JobListings" ADD COLUMN IF NOT EXISTS "AcceptedByName" character varying(120) NULL;
            """);

        var email = (configuration["SUPERUSER_EMAIL"] ?? "admin@oficiocr.local").Trim().ToLowerInvariant();
        if (await database.Users.AnyAsync(user => user.Email == email))
            return;

        var superuser = new User
        {
            Name = "Superusuario",
            Email = email,
            Role = "superusuario"
        };
        var password = configuration["SUPERUSER_PASSWORD"] ?? "AdminOficioCR!2026";
        var hasher = scope.ServiceProvider.GetRequiredService<IPasswordHasher<User>>();
        superuser.PasswordHash = hasher.HashPassword(superuser, password);

        database.Users.Add(superuser);
        await database.SaveChangesAsync();
    }
}
