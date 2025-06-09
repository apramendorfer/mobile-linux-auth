import httplib
import random
import json

DEFAULT_USER = "TODO"
BACKEND_URL = "TODO"

headers = {"Content-type": "application/json"}

random_number = random.randint(100, 999)
body = json.dumps({"userName": DEFAULT_USER, "otp": str(random_number)})


def pam_sm_authenticate(pamh, flags, argv):
    hint = pamh.Message(
        pamh.PAM_TEXT_INFO,
        "Resolve authentication request on your mobile device " + str(random_number),
    )
    pamh.conversation(hint)

    contents = httplib.HTTPConnection(BACKEND_URL)
    contents.request("POST", "/Authentication/createwait", body, headers)

    resp = contents.getresponse()
    if resp.status == 200:
        print("Successfully authenticated")
        return pamh.PAM_SUCCESS
    else:
        print("Passwordless authentication failed...")
        hint = pamh.Message(
            pamh.PAM_TEXT_INFO,
            "Passwordless authentication failed. Continue with password.",
        )
        pamh.conversation(hint)
        return pamh.PAM_AUTH_ERR


def pam_sm_setcred(pamh, flags, argv):
    return pamh.PAM_SUCCESS


def pam_sm_acct_mgmt(pamh, flags, argv):
    return pamh.PAM_SUCCESS


def pam_sm_open_session(pamh, flags, argv):
    return pamh.PAM_SUCCESS


def pam_sm_close_session(pamh, flags, argv):
    return pamh.PAM_SUCCESS


def pam_sm_chauthtok(pamh, flags, argv):
    return pamh.PAM_SUCCESS
