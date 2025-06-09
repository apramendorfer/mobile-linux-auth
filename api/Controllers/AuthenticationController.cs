using api.Models;
using api.Services;
using Microsoft.AspNetCore.Mvc;
using System.Net;

namespace api.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class AuthenticationController : ControllerBase
    {
        private readonly AuthenticationRequestsService _authenticationRequestsService;

        public AuthenticationController(AuthenticationRequestsService authenticationRequestsService)
        {
            _authenticationRequestsService = authenticationRequestsService;
        }


        [HttpGet]
        public AuthenticationRequest[] Requests(string userName)
        {
            return _authenticationRequestsService
               .GetAll()
               .Where(x => x.User.Name == userName && x.ExpiresAt > DateTime.Now)
               .OrderByDescending(x => x.Timestamp)
               .ToArray();
        }

        [HttpPost]
        public ActionResult ResolveRequest([FromBody] ResolveRequest data)
        {
            var result = _authenticationRequestsService.ResolveRequest(data.RequestId, data.SignedData);
            if (result == true)
            {
                return Ok("Authenticated");
            }
            else
            {
                return Unauthorized();
            }

        }

        [HttpPost("createwait")]
        public async Task<ActionResult> CreateWaitAuthenticationRequest(CreateAuthenticationRequest request, CancellationToken userCt)
        {
            var userAgent = Request.Headers;
            var user = _authenticationRequestsService._users.Find(x => x.Name == request.UserName);
            if (user == null)
            {
                return NotFound();
            }
            var authRequest = _authenticationRequestsService.CreateRequest(user, request.Otp);

            var timer = new PeriodicTimer(TimeSpan.FromSeconds(1));

            var cts = CancellationTokenSource.CreateLinkedTokenSource(userCt);
            cts.CancelAfter(TimeSpan.FromSeconds(30));

            while (!cts.IsCancellationRequested)
            {
                if (_authenticationRequestsService.IsRequestResolved(authRequest.Uuid))
                {
                    return Ok();
                }

                // smaller delay makes server more responsive, but decreases performance
                await Task.Delay(100);
            }

            return Unauthorized();
        }



        // Monitoring Endpoints
        [HttpPost("settype")]
        public ActionResult SetType(Param param)
        {
            _authenticationRequestsService.SetType(param.Type);
            return Ok();
        }


        [HttpGet("report")]
        public ActionResult Report()
        {
            return Ok(_authenticationRequestsService.GetAllResolved().GroupBy(x => x.Type).Select(group => new NewRecord(
                group.Key,
                group.Average(d => (d.ResolvedAt - d.Timestamp).Value.Seconds)
            )));
          
        }

    }

    public record Param(string Type);

    internal record NewRecord(string Type, double AverageDiff);
}
