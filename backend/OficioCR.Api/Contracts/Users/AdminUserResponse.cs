namespace OficioCR.Api.Contracts.Users;

public sealed record AdminUserResponse(Guid Id, string Name, string Email, string Role, DateTimeOffset CreatedAt);
