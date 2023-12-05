const getUsers = (onSuccess, onError) =>
    $.ajax({
        url: "/api/users",
        type: "get",
        success: response => onSuccess(response),
        error: xhr => onError(xhr)
    });