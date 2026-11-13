using MailKit;
using MailKit.Net.Smtp;
using MailKit.Security;
using MimeKit;

namespace OficioCR.Api.Security;

/// <summary>Entrega correo por SMTP con STARTTLS de forma asíncrona.</summary>
public sealed class EmailSender(IConfiguration configuration, IWebHostEnvironment environment, ILogger<EmailSender> logger)
{
    public async Task SendAsync(string recipient, string subject, string body)
    {
        var host = configuration["SMTP_HOST"];
        var username = configuration["SMTP_USERNAME"];
        var password = configuration["SMTP_PASSWORD"];
        if (string.IsNullOrWhiteSpace(host) || string.IsNullOrWhiteSpace(username) || string.IsNullOrWhiteSpace(password))
        {
            if (environment.IsDevelopment())
            {
                Console.WriteLine($"[correo de desarrollo] Para: {recipient} | {subject}\n{body}");
                return;
            }
            throw new InvalidOperationException("Configura SMTP_HOST, SMTP_USERNAME y SMTP_PASSWORD para enviar correo.");
        }

        var message = new MimeMessage();
        var from = configuration["SMTP_FROM"];
        message.From.Add(MailboxAddress.Parse(string.IsNullOrWhiteSpace(from) ? username : from));
        message.To.Add(MailboxAddress.Parse(recipient));
        message.Subject = subject;
        message.Body = new BodyBuilder { HtmlBody = body }.ToMessageBody();

        var port = configuration.GetValue("SMTP_PORT", 587);
        var useTls = configuration.GetValue("SMTP_SSL", true);
        var socketOptions = useTls
            ? (port == 465 ? SecureSocketOptions.SslOnConnect : SecureSocketOptions.StartTls)
            : SecureSocketOptions.Auto;

        using var client = new SmtpClient();
        try
        {
            await client.ConnectAsync(host, port, socketOptions);
            await client.AuthenticateAsync(username, password.Trim());
            await client.SendAsync(message);
            await client.DisconnectAsync(true);
        }
        catch (Exception exception)
        {
            // Mantén el detalle SMTP en los logs, sin registrar credenciales ni el contenido del mensaje.
            logger.LogError(exception, "SMTP delivery failed for host {SmtpHost} on port {SmtpPort}", host, port);
            var serverMessage = exception.ToString();
            if (serverMessage.Contains("Username and Password not accepted", StringComparison.OrdinalIgnoreCase)
                || serverMessage.Contains("BadCredentials", StringComparison.OrdinalIgnoreCase))
                throw new InvalidOperationException("Google rechazó la autenticación SMTP. Usa la cuenta principal de Google Workspace en SMTP_USERNAME y una contraseña de aplicación activa en SMTP_PASSWORD; si el remitente es un alias, configúralo en SMTP_FROM.", exception);
            throw new InvalidOperationException("No se pudo completar el envío SMTP. Consulta el log de la API para conocer el error del servidor.", exception);
        }
    }
}
