using System.Security.Cryptography;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.Extensions.Caching.Distributed;

namespace OficioCR.Api.Security;

/// <summary>Guarda el ticket en cache y entrega al navegador solo una referencia aleatoria protegida.</summary>
public sealed class DistributedCacheTicketStore(IDistributedCache cache) : ITicketStore
{
    private const string KeyPrefix = "auth-session:";
    public async Task<string> StoreAsync(AuthenticationTicket ticket)
    {
        var key = Convert.ToHexString(RandomNumberGenerator.GetBytes(32));
        await SaveAsync(key, ticket);
        return key;
    }

    public Task RenewAsync(string key, AuthenticationTicket ticket) => SaveAsync(key, ticket);

    public async Task<AuthenticationTicket?> RetrieveAsync(string key)
    {
        var value = await cache.GetAsync(KeyPrefix + key);
        return value is null ? null : TicketSerializer.Default.Deserialize(value);
    }

    public Task RemoveAsync(string key) => cache.RemoveAsync(KeyPrefix + key);

    private Task SaveAsync(string key, AuthenticationTicket ticket)
    {
        var expiry = ticket.Properties.ExpiresUtc ?? DateTimeOffset.UtcNow.Add(SessionPolicy.Lifetime);
        return cache.SetAsync(KeyPrefix + key, TicketSerializer.Default.Serialize(ticket), new DistributedCacheEntryOptions
        {
            AbsoluteExpiration = expiry
        });
    }
}
