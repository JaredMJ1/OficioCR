using Microsoft.EntityFrameworkCore;
using OficioCR.Api.Models;

namespace OficioCR.Api.Data;

public sealed class AppDbContext(DbContextOptions<AppDbContext> options) : DbContext(options)
{
    public DbSet<User> Users => Set<User>();
    public DbSet<JobListing> JobListings => Set<JobListing>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        var user = modelBuilder.Entity<User>();
        user.HasKey(item => item.Id);
        user.HasIndex(item => item.Email).IsUnique();
        user.Property(item => item.Name).HasMaxLength(120).IsRequired();
        user.Property(item => item.Email).HasMaxLength(254).IsRequired();
        user.Property(item => item.PasswordHash).IsRequired();
        user.Property(item => item.Role).HasMaxLength(30).IsRequired();
        var job = modelBuilder.Entity<JobListing>();
        job.HasKey(item => item.Id);
        job.Property(item => item.Title).HasMaxLength(120).IsRequired();
        job.Property(item => item.Description).HasMaxLength(2000).IsRequired();
        job.Property(item => item.Category).HasMaxLength(60).IsRequired();
        job.Property(item => item.Location).HasMaxLength(100).IsRequired();
        job.Property(item => item.AuthorName).HasMaxLength(120).IsRequired();
        job.HasIndex(item => item.CreatedAt);
    }
}
