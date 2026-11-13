using System.Security.Cryptography;
using System.Net;
using System.Text;
using Microsoft.Extensions.Caching.Distributed;

namespace OficioCR.Api.Security;

/// <summary>Tokens temporales de un solo uso. Solo se guarda en cache el hash del token.</summary>
public sealed class AccountWorkflowService(IDistributedCache cache, EmailSender email, IConfiguration configuration)
{
    private static readonly TimeSpan TokenLifetime = TimeSpan.FromHours(24);

    public async Task SendPasswordResetAsync(string address, string name)
    {
        var token = await IssueAsync("reset", address);
        var link = BuildLink("/recuperar-contrasena", address, token);
        await email.SendAsync(address, "Recupera tu acceso a OficioCR",
            $"<p>Hola {WebUtility.HtmlEncode(name)},</p><p>Usa este código temporal para cambiar tu contraseña:</p><p><strong>{token}</strong></p><p>También puedes abrir <a href=\"{link}\">la página de recuperación</a>. Vence en 24 horas.</p>");
    }

    public async Task<bool> ConsumeAsync(string purpose, string address, string token)
    {
        if (string.IsNullOrWhiteSpace(token) || token.Length != 64) return false;
        var key = Key(purpose, address, token);
        var value = await cache.GetStringAsync(key);
        if (value is null) return false;
        await cache.RemoveAsync(key);
        return CryptographicOperations.FixedTimeEquals(Encoding.UTF8.GetBytes(value), Encoding.UTF8.GetBytes(address.ToLowerInvariant()));
    }

    private async Task<string> IssueAsync(string purpose, string address)
    {
        var token = Convert.ToHexString(RandomNumberGenerator.GetBytes(32));
        await cache.SetStringAsync(Key(purpose, address, token), address.ToLowerInvariant(), new DistributedCacheEntryOptions
        {
            AbsoluteExpirationRelativeToNow = TokenLifetime
        });
        return token;
    }

    private string BuildLink(string path, string address, string token)
    {
        // CORS acepta varios orígenes separados por coma; el enlace debe usar solo el primero.
        var origin = (configuration["FRONTEND_ORIGIN"] ?? "http://localhost:4200")
            .Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries)[0];
        return $"{origin.TrimEnd('/')}{path}?email={Uri.EscapeDataString(address)}&token={Uri.EscapeDataString(token)}";
    }

    private static string Key(string purpose, string address, string token)
    {
        var digest = Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(token)));
        return $"account:{purpose}:{address.ToLowerInvariant()}:{digest}";
    }
}
