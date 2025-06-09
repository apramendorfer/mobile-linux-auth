namespace api.Models
{
    public class NotificationModel
    {
        public string Title { get; set; }
        public string Body { get; set; }
        public string DeviceToken { get; set; }
        public Guid RequestId { get; set; }
        public string Challenge { get; set; }
        public string Otp { get; set; }
    }
}
