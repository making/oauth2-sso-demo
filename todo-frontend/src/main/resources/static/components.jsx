// Container for the whole TodoList app
function Container({children}) {
    return (<div style={{maxWidth: '800px', margin: '0 auto', fontFamily: 'Arial, sans-serif'}}>
            {children}
        </div>);
}

// Header component for title
function Header({children}) {
    return (<h1 style={{textAlign: 'center', color: '#333', marginBottom: '20px'}}>
            {children}
        </h1>);
}

// Styled button with hover effect
function StyledButton({children, onClick, type = 'button', style}) {
    return (<button
            type={type}
            onClick={onClick}
            style={{
                padding: '10px 20px',
                fontSize: '16px',
                backgroundColor: '#28a745',
                color: '#fff',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                transition: 'background-color 0.3s', ...style,
            }}
            onMouseEnter={(e) => e.target.style.backgroundColor = '#218838'}
            onMouseLeave={(e) => e.target.style.backgroundColor = '#28a745'}
        >
            {children}
        </button>);
}

// Small icon button for action controls (toggle and delete)
function IconButton({icon, color, onClick, title}) {
    return (<button
            onClick={onClick}
            title={title}
            style={{
                backgroundColor: 'transparent',
                border: 'none',
                cursor: 'pointer',
                fontSize: '16px',
                color: color,
                margin: '0 5px',
            }}
        >
            {icon}
        </button>);
}

// Input field for entering todo title
function StyledInput({value, onChange, placeholder}) {
    return (<input
            type="text"
            value={value}
            onChange={onChange}
            placeholder={placeholder}
            required={true}
            style={{
                padding: '10px',
                width: '70%',
                fontSize: '16px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                marginRight: '10px',
            }}
        />);
}

// Table container with header and body styling
function StyledTable({children}) {
    return (<table style={{width: '100%', borderCollapse: 'collapse', boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)'}}>
            {children}
        </table>);
}

// Table cell component with optional width
function TableCell({children, header = false, center = false, width}) {
    const baseStyle = {
        padding: '10px',
        textAlign: center ? 'center' : 'left',
        backgroundColor: header ? '#f4f4f4' : 'inherit',
        borderBottom: header ? '2px solid #ddd' : '1px solid #ddd',
        width: width || 'auto',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
    };

    return header ? (<th style={baseStyle}>{children}</th>) : (<td style={baseStyle}>{children}</td>);
}

// WelcomeMessage component to display the username
function WelcomeMessage({username}) {
    return (<p style={{textAlign: 'center', fontSize: '18px', color: '#555'}}>
            Welcome, {username}!
        </p>);
}