function modifyProfile(memberId, accessToken, refreshToken, updateDto) {
    if (updateDto) {
        console.log(updateDto);
        $.ajax({
            method: "PUT",
            url: "/api/members/" + memberId,
            data: JSON.stringify(updateDto),
            async: false,
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Content-type", "application/json");
                xhr.setRequestHeader("X-AUTH-TOKEN", accessToken);
            },
            contentType: "application/json",
            statusCode: {
                200: function (response) {
                    alert("프로필 수정이 정상적으로 완료되었습니다.");
                    location.reload();
                },
                400: function (response) { // JoinId 나 Nickname 중복, 본인확인 비밀번호 틀림, validation Error
                    console.log(response);
                    let responseJson = JSON.parse(response.responseText);
                    if (responseJson.message != undefined) {
                        alert(responseJson.message);
                    } else {
                        let str = "";
                        for (let i = 0; i < responseJson.length; i++) {
                            str += responseJson[i].defaultMessage + "\n";
                        }

                        alert(str);

                    }

                    $('#modifyConfirmPassword').val("");
                    $('#modifyConfirmationModal').modal("hide");
                },
                403: function () { // Access 실패
                    let refreshResult = refresh(refreshToken);
                    if (refreshResult.state == 200) {
                        modifyProfile(refreshResult.memberId, refreshResult.accessToken, refreshResult.refreshToken, updateDto);
                    } else {
                        ifRefreshFail();
                    }
                }
            }
        });
    }
}

function getUpdateDto() {
    const joinId = $('#joinId').val();
    const email = $('#email').val();
    const name = $('#name').val();
    const nickname = $('#nickname').val();
    let year = $('select[name=year]').val();
    let month = $('select[name=month]').val();
    let day = $('select[name=day]').val();
    const dateOfBirth = year + "-" + month + "-" + day;
    const gender = $('select[name=gender]').val();
    const simpleAddress = $('#simpleAddress').val();
    const confirmPassword = $('#modifyConfirmPassword').val();

    if (year == -1 || month == -1 || day == -1) {
        alert("생년월일을 선택해주세요.");
        $('#modifyConfirmationModal').modal('hide');
        return null;
    }

    if (gender == -1) {
        alert("셩별을 선택해주세요.");
        $('#modifyConfirmationModal').modal('hide');
        return null;
    }

    if (confirmPassword == "" || confirmPassword == " ") {
        alert("본인확인을 위한 비밀번호를 입력해 주세요.");
        return null;
    }

    return {
        joinId: joinId,
        password: confirmPassword,
        email: email,
        name: name,
        nickname: nickname,
        dateOfBirth: dateOfBirth,
        gender: gender,
        simpleAddress: simpleAddress
    };
}

function switchToNormalMode() {
    $('#modify-btn').prop("type", "button");
    $('#withdrawal-btn').prop("type", "button");
    $('#cancel-btn').prop("type", "hidden");
    $('#confirm-btn').prop("type", "hidden");

    $('#joinId').prop('readOnly', true);
    $('#email').prop('readOnly', true);
    $('#name').prop('readOnly', true);
    $('#nickname').prop('readOnly', true);
    $('#simpleAddress').prop('readOnly', true);

    $('#selectDateOfBirth').css('display', 'none');
    $('#textDateOfBirth').css('display', 'block');

    $('#textGender').css('display', 'block');
    $('#selectGender').css('display', 'none');
}

function switchToModifyMode() {
    $('#modify-btn').prop("type", "hidden");
    $('#withdrawal-btn').prop("type", "hidden");
    $('#cancel-btn').prop("type", "button");
    $('#confirm-btn').prop("type", "button");

    $('#joinId').prop('readOnly', false);
    $('#email').prop('readOnly', false);
    $('#name').prop('readOnly', false);
    $('#nickname').prop('readOnly', false);
    $('#simpleAddress').prop('readOnly', false);

    $('#selectDateOfBirth').css('display', 'block');
    $('#textDateOfBirth').css('display', 'none');

    $('select[name=year]').val(-1);
    $('select[name=month]').val(-1);
    $('select[name=day]').val(-1);

    $('select[name=gender]').val(-1);

    $('#selectGender').css('display', 'block');
    $('#textGender').css('display', 'none');
}

function fillInProfile(profile) {
    $('#memberId').val(profile.id);
    $('#joinId').val(profile.joinId);
    $('#email').val(profile.email);
    $('#name').val(profile.name);
    $('#nickname').val(profile.nickname);
    $('#dateOfBirth').val(new Date(profile.dateOfBirth).toLocaleDateString());
    $('#gender').val(getKoreanGender(profile.gender));
    $('#simpleAddress').val(profile.simpleAddress);
    $('#sharedVocabularyCount').val(profile.sharedVocabularyCount);
    $('#bbsCount').val(profile.bbsCount);
    $('#registerDate').val(new Date(profile.registerDate).toLocaleString());
    $('#updateDate').val(new Date(profile.updateDate).toLocaleString());
}

function getKoreanGender(gender) {
    let koreanGender;
    if (gender == "MALE") {
        koreanGender = "남성";
    } else {
        koreanGender = "여성";
    }

    return koreanGender;
}

function getProfile(memberId, accessToken, refreshToken) {
    console.log("get member profile.");

    let id = null;
    let joinId = null;
    let email = null;
    let name = null;
    let nickname = null;
    let dateOfBirth = null;
    let gender = null;
    let simpleAddress = null;
    let sharedVocabularyCount = null;
    let bbsCount = null;
    let registerDate = null;
    let updateDate = null;

    $.ajax({
        method: "GET",
        url: "/api/members/" + memberId,
        beforeSend: function (xhr) {
            xhr.setRequestHeader("Content-type", "application/json");
            xhr.setRequestHeader("X-AUTH-TOKEN", accessToken);
        },
        async: false,
        statusCode: {
            200: function (response) {
                console.log("Get profile success!!");

                id = response.data.id;
                joinId = response.data.joinId;
                email = response.data.email;
                name = response.data.name;
                nickname = response.data.nickname;
                dateOfBirth = response.data.dateOfBirth;
                gender = response.data.gender;
                simpleAddress = response.data.simpleAddress;
                sharedVocabularyCount = response.data.sharedVocabularyCount;
                bbsCount = response.data.bbsCount;
                registerDate = response.data.registerDate;
                updateDate = response.data.updateDate;
            },
            403: function () {
                console.log("Access fail...");
                let refreshResult = refresh(refreshToken);
                console.log(refreshResult);

                if (refreshResult.state == 200) {
                    let profile = getProfile(refreshResult.memberId, refreshResult.accessToken, refreshToken.refreshToken);

                    id = profile.id;
                    joinId = profile.joinId;
                    email = profile.email;
                    name = profile.name;
                    nickname = profile.nickname;
                    dateOfBirth = profile.dateOfBirth;
                    gender = profile.gender;
                    simpleAddress = profile.simpleAddress;
                    sharedVocabularyCount = profile.sharedVocabularyCount;
                    bbsCount = profile.bbsCount;
                    registerDate = profile.registerDate;
                    updateDate = profile.updateDate;

                } else {
                    ifRefreshFail();
                }
            },
            404: function (response) {
                alert("잘 못된 요청입니다. 해당 회원을 찾을 수 없습니다.");
                localStorage.clear();
                $(location).attr('href', '/');
            },
            401: function (response) {
                alert("잘 못된 요청입니다. 다른 회원의 정보는 조회할 수 없습니다.");
                localStorage.clear();
                $(location).attr('href', '/');
            }
        }
    });

    return {
        id: id,
        joinId: joinId,
        email: email,
        name: name,
        nickname: nickname,
        dateOfBirth: dateOfBirth,
        gender: gender,
        simpleAddress: simpleAddress,
        sharedVocabularyCount: sharedVocabularyCount,
        bbsCount: bbsCount,
        registerDate: registerDate,
        updateDate: updateDate
    }
}