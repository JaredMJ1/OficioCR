namespace OficioCR.Api.Contracts.Auth;

public sealed record RegisterRequest(string? Name, string? Email, string? Password);
