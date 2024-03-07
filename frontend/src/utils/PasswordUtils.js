export const isValidPassword = (password, setErrorMessage) => {
    // password must contain:
    // * at least 8 characters
    // * at least one uppercase letter
    // * at least one lowercase letter
    // * at least one digit
    // * at least one special character from !"#$%&'()*+,-./\:;<=>?@[]^_`{|}~

    const atLeast8Chars = password.length >= 8;
    const containsLowercase = password.match("(?=.*?[a-z])");
    const containsUppercase = password.match("(?=.*?[A-Z])");
    const containsDigit = password.match("(?=.*?[0-9])");
    const containsSpecialChar = password.match("(?=.*?[!\"#$%&'()*+,-./\\\\:;<=>?@\\[\\]^_`{|}~])");

    let passwordIsValid = false;

    if (!atLeast8Chars) setErrorMessage("Must be at least 8 characters long");
    else if (!containsLowercase) setErrorMessage("Must contain a lowercase letter");
    else if (!containsUppercase) setErrorMessage("Must contain an uppercase letter");
    else if (!containsDigit) setErrorMessage("Must contain a digit");
    else if (!containsSpecialChar) setErrorMessage("Must contain a special character");
    else passwordIsValid = true;

    return passwordIsValid;
}
