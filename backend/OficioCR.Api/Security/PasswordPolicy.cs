namespace OficioCR.Api.Security;

public static class PasswordPolicy
{
    public static bool IsValid(string? password)
    {
        if (string.IsNullOrEmpty(password) || password.Length is < 10 or > 128)
            return false;

        return password.Any(character => character is >= 'A' and <= 'Z')
            && password.Any(character => character is >= 'a' and <= 'z')
            && password.Any(character => character is >= '0' and <= '9')
            && password.Any(IsAsciiSymbol);
    }

    private static bool IsAsciiSymbol(char character) =>
        character is >= '!' and <= '/' or >= ':' and <= '@' or >= '[' and <= '`' or >= '{' and <= '~';
}
