import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    primary: {
      main: '#071E4E', // Advance Blue
    },
    secondary: {
      main: '#ED1E79', // Advance Pink
    },
    error: {
      main: '#B00020', // Danger
    },
    warning: {
      main: '#FFC470', // Warning
    },
    info: {
      main: '#662D91', // Advance Purple
    },
    success: {
      main: '#006837', // Success
    },
    background: {
      default: '#FFFFFF', // White
      paper: '#FAFAFA', // Grey-50
    },
    text: {
      primary: 'rgba(0, 0, 0, 0.87)', // Black 87%
      secondary: 'rgba(0, 0, 0, 0.4)' // Black 40%
    },
  },
  typography: {
    fontFamily: 'Roboto',
    h1: {
      fontWeight: 'regular',
      fontSize: '24px',
      letterSpacing: '0px',
    },
    h2: {
      fontWeight: 'medium',
      fontSize: '20px',
      letterSpacing: '0px',
    },
    h3: {
      fontWeight: 'regular',
      fontSize: '16px',
      letterSpacing: '0px',
    },
    h4: {
      fontWeight: 'medium',
      fontSize: '14px',
      letterSpacing: '0px',
    },
    body1: {
      fontWeight: 'regular',
      fontSize: '16px',
      letterSpacing: '0px',
    },
    body2: {
      fontWeight: 'regular',
      fontSize: '14px',
      letterSpacing: '0px',
    },
    button: {
      fontWeight: 'medium',
      fontSize: '14px',
      letterSpacing: '0px',
    },
    caption: {
      fontWeight: 'regular',
      fontSize: '12px',
      letterSpacing: '0px',
    },
  },
});

export default theme;
