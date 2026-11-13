using OficioCR.Api.Contracts.Users;

namespace OficioCR.Api.Contracts.Auth;

public sealed record SessionResponse(DateTimeOffset ExpiresAt, UserResponse User);
