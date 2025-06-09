using api.Models;
using System.Security.Cryptography;

namespace api.Services
{
    public class AuthenticationRequestsService
    {

        private string _currentType = "unknown";
        private readonly List<AuthenticationRequest> _authenticationRequests = new List<AuthenticationRequest>();
        public readonly List<User> _users = [];
        public AuthenticationRequestsService()
        {
            // seed users here
        }

        // Can be used to identify requests later
        public void SetType(string type)
        {
            _currentType = type;
        }

        public AuthenticationRequest[] GetAll()
        {
            return _authenticationRequests.Where(x => x.Resolved == false).ToArray();
        }

        public AuthenticationRequest[] GetAllResolved()
        {
            return _authenticationRequests.Where(x => x.Resolved == true).ToArray();
        }


        private string CreateChallenge()
        {
            byte[] nonce = new byte[64];

            using (RandomNumberGenerator rng = RandomNumberGenerator.Create())
            {

                rng.GetBytes(nonce);
            }

            return BitConverter.ToString(nonce).Replace("-", "");
        }

        public AuthenticationRequest CreateRequest(User user, string otp)
        {
            var request = new AuthenticationRequest()
            {
                Challenge = CreateChallenge(),
                User = user,
                Otp = otp
            };

            _authenticationRequests.Add(request);
            _ = new NotificationService().SendNotification(new NotificationModel()
            {
                Title = "Authentication Request",
                Body = "Approve authentication request?",
                DeviceToken = user.DeviceToken,
                Challenge = request.Challenge,
                Otp = request.Otp,
                RequestId = request.Uuid
            }); 
            return request;
        }

        public bool IsRequestResolved(Guid uuid)
        {
            return _authenticationRequests.Any(x => x.Uuid == uuid && x.Resolved == true);
        }

        public bool ResolveRequest(Guid requestId, string signedData)
        {
            var request = _authenticationRequests.Find(x => x.Uuid == requestId && x.ExpiresAt > DateTime.Now);

            if (request != null)
            {
                var result = request.User.Verify(Convert.FromBase64String(signedData), request.Challenge);
                request.Resolved = result;
                request.Resolved = true;
                request.ResolvedAt = DateTime.Now;
                request.Type = _currentType;
                return result;
            }
            else
            {
                return false;
            }
        }
    }
}
