namespace OficioCR.Api.Security;

public static class SessionPolicy
{
    public static readonly TimeSpan Lifetime = TimeSpan.FromMinutes(30);
}
