import {ReactNode, CSSProperties, ChangeEvent} from 'react';

// Container for the whole TodoList app
interface ContainerProps {
    children: ReactNode;
}

const Container = ({children}: ContainerProps) => (
    <div style={{maxWidth: '800px', margin: '0 auto', fontFamily: 'Arial, sans-serif'}}>
        {children}
    </div>
);

// Header component for title
interface HeaderProps {
    children: ReactNode;
}

const Header = ({children}: HeaderProps) => (
    <h1 style={{textAlign: 'center', color: '#333', marginBottom: '20px'}}>
        {children}
    </h1>
);

// Styled button with hover effect
interface StyledButtonProps {
    children: ReactNode;
    onClick?: () => void;
    type?: 'button' | 'submit' | 'reset';
    style?: CSSProperties;
}

const StyledButton = ({children, onClick, type = 'button', style}: StyledButtonProps) => (
    <button
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
            transition: 'background-color 0.3s',
            ...style,
        }}
        onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = '#218838')}
        onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = '#28a745')}
    >
        {children}
    </button>
);

// Small icon button for action controls (toggle and delete)
interface IconButtonProps {
    icon: ReactNode;
    color: string;
    onClick: () => void;
    title: string;
}

const IconButton = ({icon, color, onClick, title}: IconButtonProps) => (
    <button
        onClick={onClick}
        title={title}
        style={{
            backgroundColor: 'transparent',
            border: 'none',
            cursor: 'pointer',
            fontSize: '16px',
            color,
            margin: '0 5px',
        }}
    >
        {icon}
    </button>
);

// Input field for entering todo title
interface StyledInputProps {
    value: string;
    onChange: (e: ChangeEvent<HTMLInputElement>) => void;
    placeholder?: string;
}

const StyledInput = ({value, onChange, placeholder}: StyledInputProps) => (
    <input
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
    />
);

// Table container with header and body styling
interface StyledTableProps {
    children: ReactNode;
}

const StyledTable = ({children}: StyledTableProps) => (
    <table
        style={{
            width: '100%',
            borderCollapse: 'collapse',
            boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
        }}
    >
        {children}
    </table>
);

// Table cell component with optional width
interface TableCellProps {
    children: ReactNode;
    header?: boolean;
    center?: boolean;
    width?: string;
}

const TableCell = ({children, header = false, center = false, width}: TableCellProps) => {
    const baseStyle: CSSProperties = {
        padding: '10px',
        textAlign: center ? 'center' : 'left',
        backgroundColor: header ? '#f4f4f4' : 'inherit',
        borderBottom: header ? '2px solid #ddd' : '1px solid #ddd',
        width: width || 'auto',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
    };

    return header ? (
        <th style={baseStyle}>{children}</th>
    ) : (
        <td style={baseStyle}>{children}</td>
    );
};

// WelcomeMessage component to display the username
interface WelcomeMessageProps {
    username: string;
}

const WelcomeMessage = ({username}: WelcomeMessageProps) => (
    <p style={{textAlign: 'center', fontSize: '18px', color: '#555'}}>
        Welcome, {username}!
    </p>
);

export {
    Container,
    Header,
    StyledButton,
    IconButton,
    StyledInput,
    StyledTable,
    TableCell,
    WelcomeMessage,
};
