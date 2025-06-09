namespace api.Models
{
    public class ResolveRequest
    {
        public Guid RequestId { get; set; }
        public string SignedData { get; set; }
    }
}
