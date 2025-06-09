using api.Models;
using FirebaseAdmin.Messaging;
using Microsoft.Extensions.Options;
using System.Collections.ObjectModel;
using System.Net.Http.Headers;
using System.Runtime;

namespace api.Services
{
    public interface INotificationService
    {
        Task<NotificationResponseModel> SendNotification(NotificationModel notificationModel);
    }


    public class NotificationService : INotificationService
    {

        public async Task<NotificationResponseModel> SendNotification(NotificationModel notification)
        {
            var data = new Dictionary<string, string>
            {
                { "RequestId", notification.RequestId.ToString() },
                { "Challenge", notification.Challenge },
                { "Otp", notification.Otp },
                { "Type", "NEW_REQUEST" }
            };
            
            var message = new Message()
            {
                Data = new ReadOnlyDictionary<string, string>(data),
                Token = notification.DeviceToken,
            };

            var messaging = FirebaseMessaging.DefaultInstance;
            var result = await messaging.SendAsync(message);


            return string.IsNullOrEmpty(result) ? new NotificationResponseModel()
            {
                IsSuccess = false,
                Message = "Message could not be sent"
            } : new NotificationResponseModel()
            {
                IsSuccess = true,
                Message = result,
            };
        }
    }
}
