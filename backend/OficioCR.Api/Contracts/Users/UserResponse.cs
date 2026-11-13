namespace OficioCR.Api.Contracts.Users;

public sealed record UserResponse(Guid Id, string Name, string Email, string Role);
