function refresh(refreshToken) {
    console.log("Try refresh");
    const onlyTokenDto = {
        refreshToken: refreshToken
    }

    let state = null;
    let memberId = null;
    let accessToken = null;


    $.ajax({
        method: "POST",
        url: "/api/refresh",
        data: JSON.stringify(onlyTokenDto),
        contentType: "application/json",
        async: false,
        statusCode: {
            200: function (response) {
                console.log("Refresh success!!");
                state = 200;
                accessToken = response.data.accessToken;
                memberId = response.data.memberId;
            },
            401: function (response) {
                console.log("Refresh fail...");
                let responseJson = JSON.parse(response.responseText);
                let message = responseJson.message;
                console.log(message);

                state = 401;
            }
        }
    });

    return {
        state: state,
        memberId: memberId,
        accessToken: accessToken,
        refreshToken: refreshToken
    }
}

function getTokenInfo() {
    console.log("Get token info.");
    let accessToken = localStorage.getItem('accessToken');
    let memberId = localStorage.getItem('memberId');
    let refreshToken = localStorage.getItem('refreshToken');
    let tokenLocation = "LS";

    if (accessToken == null) {
        console.log("Local storage is null.");
        console.log("Get token info from session storage.");

        accessToken = sessionStorage.getItem('accessToken');
        memberId = sessionStorage.getItem('memberId');
        refreshToken = sessionStorage.getItem('refreshToken');
        tokenLocation = "SS";
    }

    return {
        accessToken: accessToken,
        memberId: memberId,
        refreshToken: refreshToken,
        tokenLocation: tokenLocation
    };
}

function storageClear() {
    console.log("Delete tokens");
    localStorage.removeItem('memberId');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    sessionStorage.removeItem('memberId');
    sessionStorage.removeItem('accessToken');
    sessionStorage.removeItem('refreshToken');
}

function ifRefreshFail() {
    alert("Refresh token이 유효하지 않습니다.");
    storageClear();
    $(location).attr('href', '/');
}