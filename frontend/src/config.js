const API_URL = process.env.REACT_APP_API_URL;
const ALLOWED_EMAIL_DOMAINS = process.env.REACT_APP_ALLOWED_EMAIL_DOMAINS.split(", ");

const config = {
    API_URL,
    ALLOWED_EMAIL_DOMAINS
};

export default config;