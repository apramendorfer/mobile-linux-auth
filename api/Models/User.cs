using System.Security.Cryptography;
using System.Text;

namespace api.Models
{
    public class User
    {
        public string Name { get; set; }
        public string FullName { get; set; }
        public string[] PublicKeys { get; set; }

        public string DeviceToken { get; set; }

        public bool Verify(byte[] signedData, string nonce)
        {
            using var rsa = new RSACryptoServiceProvider();
            return PublicKeys.Any(key =>
            {
                rsa.ImportFromPem(key);
                SHA256 sha256 = SHA256.Create();
                byte[] hash = sha256.ComputeHash(Encoding.UTF8.GetBytes(nonce));

                return rsa.VerifyHash(hash, CryptoConfig.MapNameToOID("SHA256"), signedData);
            });
        }
    }
}
