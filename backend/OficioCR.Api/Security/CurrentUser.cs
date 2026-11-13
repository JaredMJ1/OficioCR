using System.Security.Claims;

namespace OficioCR.Api.Security;

public sealed class CurrentUser(IHttpContextAccessor contextAccessor)
{
    /// <summary>
    /// Devuelve los datos que el inicio de sesión guardó en la identidad de esta petición.
    /// La cookie la valida ASP.NET Core; aquí solo leemos sus claims ya autenticados.
    /// </summary>
    public CurrentUserData? Get()
    {
        var principal = contextAccessor.HttpContext?.User;

        // Sin una petición activa o sin un ID válido, no hay un usuario que podamos asociar.
        if (!Guid.TryParse(principal?.FindFirstValue(ClaimTypes.NameIdentifier), out var id)) return null;

        return new CurrentUserData(id,
            principal?.FindFirstValue(ClaimTypes.Name),
            principal?.FindFirstValue(ClaimTypes.Email),
            principal?.FindFirstValue(ClaimTypes.Role));
    }
}

// Un objeto sencillo para que los endpoints reciban todos los datos del usuario de una vez.
public sealed record CurrentUserData(Guid Id, string? Name, string? Email, string? Role);
