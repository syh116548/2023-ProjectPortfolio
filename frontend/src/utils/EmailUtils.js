import config from '../config.js';

export const { ALLOWED_EMAIL_DOMAINS } = config;

export const isValidEmailFormat = (email) => {
    const regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    return email.match(regex) != null;
}

export const hasAllowedEmailDomain = (email) => {
    for (let domain of ALLOWED_EMAIL_DOMAINS) {
        if (email.endsWith("@" + domain)) {
            return true;
        }
    }
    return false;
}
