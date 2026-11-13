using System.Text.RegularExpressions;

namespace OficioCR.Api.Security;

public static partial class UserInput
{
    [GeneratedRegex(@"^[^\s@]+@[^\s@]+\.[^\s@]{2,}$")]
    private static partial Regex EmailPattern();

    public static bool TryNormalize(string? nameInput, string? emailInput, out string name, out string email)
    {
        name = nameInput?.Trim() ?? string.Empty;
        email = emailInput?.Trim().ToLowerInvariant() ?? string.Empty;
        return !string.IsNullOrWhiteSpace(name) && name.Length <= 120
            && email.Length <= 254 && EmailPattern().IsMatch(email);
    }
}
