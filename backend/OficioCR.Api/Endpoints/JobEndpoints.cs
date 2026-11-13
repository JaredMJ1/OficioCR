using Microsoft.EntityFrameworkCore;
using OficioCR.Api.Data;
using OficioCR.Api.Models;

namespace OficioCR.Api.Endpoints;

public static class JobEndpoints
{
    public static IEndpointRouteBuilder MapJobEndpoints(this IEndpointRouteBuilder endpoints)
    {
        endpoints.MapGet("/api/jobs", SearchAsync);
        endpoints.MapPost("/api/jobs", CreateAsync).RequireAuthorization();
        endpoints.MapPost("/api/jobs/{id:guid}/accept", AcceptAsync).RequireAuthorization();
        return endpoints;
    }

    private static async Task<IResult> SearchAsync(string? search, AppDbContext database)
    {
        var query = database.JobListings.AsNoTracking();
        if (!string.IsNullOrWhiteSpace(search))
        {
            var term = search.Trim();
            query = query.Where(item => item.Title.Contains(term) || item.Description.Contains(term)
                || item.Category.Contains(term) || item.Location.Contains(term));
        }
        var items = await query.OrderByDescending(item => item.CreatedAt).Take(100).ToListAsync();
        return Results.Ok(items);
    }

    private static async Task<IResult> CreateAsync(CreateJobRequest request, CurrentUser currentUser, AppDbContext database)
    {
        var title = request.Title?.Trim();
        var description = request.Description?.Trim();
        var category = request.Category?.Trim();
        var location = request.Location?.Trim();
        if (string.IsNullOrWhiteSpace(title) || title.Length > 120
            || string.IsNullOrWhiteSpace(description) || description.Length > 2000
            || string.IsNullOrWhiteSpace(category) || category.Length > 60
            || string.IsNullOrWhiteSpace(location) || location.Length > 100)
            return Results.BadRequest(new { message = "Completa todos los campos respetando sus longitudes máximas." });

        var user = currentUser.Get();
        if (user is null) return Results.Unauthorized();
        var listing = new JobListing
        {
            Title = title, Description = description, Category = category, Location = location,
            AuthorId = user.Id, AuthorName = user.Name ?? "Usuario OficioCR"
        };
        database.JobListings.Add(listing);
        await database.SaveChangesAsync();
        return Results.Created($"/api/jobs/{listing.Id}", listing);
    }

    private static async Task<IResult> AcceptAsync(Guid id, CurrentUser currentUser, AppDbContext database)
    {
        var user = currentUser.Get();
        if (user is null) return Results.Unauthorized();
        var listing = await database.JobListings.SingleOrDefaultAsync(item => item.Id == id);
        if (listing is null) return Results.NotFound(new { message = "No se encontró el anuncio." });
        if (listing.AcceptedById is not null) return Results.Conflict(new { message = "Este trabajo ya fue aceptado." });
        if (listing.AuthorId == user.Id) return Results.BadRequest(new { message = "No puedes aceptar tu propio anuncio." });
        listing.AcceptedById = user.Id;
        listing.AcceptedByName = user.Name ?? "Usuario OficioCR";
        await database.SaveChangesAsync();
        return Results.Ok(listing);
    }
}

public sealed record CreateJobRequest(string? Title, string? Description, string? Category, string? Location);
