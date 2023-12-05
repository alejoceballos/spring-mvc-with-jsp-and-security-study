const displayUsers = () => {
    const tBody = $("#tbl");

    const onSuccess = response =>
        response.forEach(obj => tBody.append(
            `<tr>
                <td>${obj.username}</td>
                <td>${obj.authorities}</td>
            </tr>`)
        );

    const onError = () =>
        tBody.append(
            `<tr>
                <td colspan="2">ERROR</td>
            </tr>`
        );

    getUsers(onSuccess, onError);
};