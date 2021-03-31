$(document).ready(function () {

    getMemberInfo();
    confirmLogout();
    ifAccessTokenIsNull();

});

function confirmLogout() {
    $('#confirm-logout-btn').click(function () {
        console.log("Confirm logout.");
        console.log("Delete tokens");

        localStorage.clear();
        location.reload();
    });
}

function ifAccessTokenIsNull() {
    let accessToken = localStorage.getItem('accessToken');
    if (accessToken == null) {
        $('#user-nickname').append("Login");
        $('#userDropdown').append(
            `
            <img class="img-profile rounded-circle" src="img/undraw_rocket.svg">
            `
        );
        $('#user-dropdown-item').remove();
        $('#userDropdown').click(function () {
            $(location).attr('href', '/members/login');
        });
    }
}

function getMemberInfo() {
    let memberId = localStorage.getItem('memberId');
    let accessToken = localStorage.getItem('accessToken');
    let refreshToken = localStorage.getItem('refreshToken');

    console.log("CALL - Get member info");

    if (accessToken != null) {
        $.ajax({
            method: "GET",
            url: "/api/members/" + memberId,
            beforeSend: function (header) {
                header.setRequestHeader("X-AUTH-TOKEN", accessToken);
            },
            // async: false,
            statusCode: {
                200: function (response) {
                    console.log("Get member info success.");
                    let nickname = response.data.nickname;

                    $('#user-nickname').append(nickname);

                    let gender = response.data.gender;

                    console.log(gender);
                    if (gender == "MALE") {
                        $('#userDropdown').append(
                            `
                             <img class="img-profile rounded-circle" src="img/undraw_profile.svg">
                            `
                        );
                    }else if (gender == "FEMALE") {
                        $('#userDropdown').append(
                            `
                             <img class="img-profile rounded-circle" src="img/undraw_profile_1.svg">
                            `
                        );
                    } else {
                        $('#userDropdown').append(
                            `
                            <img class="img-profile rounded-circle" src="img/undraw_rocket.svg">
                            `
                        );
                    }
                },
                403: function () {
                    console.log("Access fail.");
                    console.log("Try refresh.");

                    refreshAndTryGetMemberInfo(refreshToken);
                },
                404: function () {
                    console.log("Member not found...");
                    console.log("Delete tokens.");

                    localStorage.clear();
                },
                401: function () {
                    alert("잘 못된 접근입니다.");

                    console.log("Wrong request!");
                    console.log("Delete tokens.");

                    localStorage.clear();
                }
            }
        });
    } else {
        console.log("Access token is null...");
    }
}

function refreshAndTryGetMemberInfo(refreshToken) {
    const onlyTokenDto = {
        refreshToken: refreshToken
    }

    $.ajax({
        method: "POST",
        url: "/api/refresh",
        data: JSON.stringify(onlyTokenDto),
        contentType: "application/json",
        statusCode: {
            200: function (response) {
                console.log("Refresh success!!");
                console.log("Register accessToken!!");
                let accessToken = response.data.accessToken;
                localStorage.setItem('accessToken', accessToken);

                getMemberInfo();
            },
            401: function (response) {
                console.log("Refresh fail...");
                let responseJson = JSON.parse(response.responseText);
                let message = responseJson.message;
                console.log(message);

                console.log("Delete tokens.");
                localStorage.clear();

                let loginConfirm = confirm("Refresh token 이 유효하지 않습니다. 로그인 하시겠습니까?");
                if (loginConfirm) {
                    console.log("Go to login page.");
                    $(location).attr('href', '/members/login');
                } else {
                    console.log("No additional authentication is performed.")
                }
            }
        }
    })

}
