namespace api.Models
{
    public class AuthenticationRequest
    {
        public string Otp { get; set; }
        public string Challenge { get; set; }
        public Guid Uuid { get;} = Guid.NewGuid();
        public string ShortName { get => Uuid.ToString().Substring(0, 4); }
        public DateTime Timestamp { get; set; } = DateTime.Now;
        public DateTime? ResolvedAt { get; set; }
        public DateTime ExpiresAt => Timestamp.AddSeconds(30);
        public User User { get; set; }
        public string Type { get; set; }
        public bool Resolved { get; set; } = false;
    }

    public class CreateAuthenticationRequest
    {
        public string UserName { get; set; }
        public string Otp { get; set; }
    }
}
