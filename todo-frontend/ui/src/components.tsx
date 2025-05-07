import { ReactNode, CSSProperties, ChangeEvent, ButtonHTMLAttributes } from 'react';

// Container for the whole TodoList app
interface ContainerProps {
    children: ReactNode;
}

const Container = ({ children }: ContainerProps) => (
    <div className="max-w-4xl mx-auto px-4 py-8 font-sans">
        {children}
    </div>
);

// Header component for title
interface HeaderProps {
    children: ReactNode;
}

const Header = ({ children }: HeaderProps) => (
    <h1 className="text-4xl font-bold text-center text-transparent bg-clip-text bg-linear-to-r from-primary-dark via-primary to-primary-light drop-shadow-xs py-2 mb-6">
        {children}
    </h1>
);

// Styled button with hover effect
interface StyledButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    children: ReactNode;
    variant?: 'primary' | 'success' | 'danger';
}

const StyledButton = ({ 
    children, 
    onClick, 
    type = 'button', 
    variant = 'primary', 
    className = '',
    ...rest 
}: StyledButtonProps) => {
    const baseClass = 'btn';
    const variantClass = `btn-${variant}`;
    
    return (
        <button
            type={type}
            onClick={onClick}
            className={`${baseClass} ${variantClass} ${className}`}
            {...rest}
        >
            {children}
        </button>
    );
};

// Small icon button for action controls (toggle and delete)
interface IconButtonProps {
    icon: ReactNode;
    onClick: () => void;
    title: string;
    variant?: 'primary' | 'success' | 'danger';
}

const IconButton = ({ 
    icon, 
    onClick, 
    title, 
    variant = 'primary' 
}: IconButtonProps) => {
    const variantClasses = {
        primary: 'text-primary hover:text-primary-dark',
        success: 'text-success hover:text-success-dark',
        danger: 'text-danger hover:text-danger-dark',
    };
    
    return (
        <button
            onClick={onClick}
            title={title}
            className={`bg-transparent border-none cursor-pointer p-2 transition-colors ${variantClasses[variant]}`}
            aria-label={title}
        >
            {icon}
        </button>
    );
};

// Input field for entering todo title
interface StyledInputProps {
    value: string;
    onChange: (e: ChangeEvent<HTMLInputElement>) => void;
    placeholder?: string;
}

const StyledInput = ({ value, onChange, placeholder }: StyledInputProps) => (
    <input
        type="text"
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required
        className="px-4 py-2 border border-gray-300 rounded-md w-full md:w-96 shadow-xs focus:outline-hidden focus:ring-2 focus:ring-primary focus:border-transparent"
    />
);

// Table container with header and body styling
interface StyledTableProps {
    children: ReactNode;
}

const StyledTable = ({ children }: StyledTableProps) => (
    <div className="w-full overflow-x-auto rounded-lg shadow-md bg-white animate-fade-in">
        <table className="w-full border-collapse">
            {children}
        </table>
    </div>
);

// Table cell component with optional width
interface TableCellProps {
    children: ReactNode;
    header?: boolean;
    center?: boolean;
    width?: string;
}

const TableCell = ({ 
    children, 
    header = false, 
    center = false, 
    width 
}: TableCellProps) => {
    // Added truncate and fixed height classes to prevent line wrapping
    const baseClass = "p-3 text-sm border-b border-gray-200 whitespace-nowrap overflow-hidden text-ellipsis";
    const alignClass = center ? "text-center" : "text-left";
    const headerClass = header ? "bg-gray-100 font-semibold text-gray-700" : "";
    
    const style: CSSProperties = {};
    if (width) {
        style.width = width;
        // Add max-width to ensure proper truncation
        style.maxWidth = width;
    }
    
    return header ? (
        <th className={`${baseClass} ${alignClass} ${headerClass}`} style={style}>
            {children}
        </th>
    ) : (
        <td className={`${baseClass} ${alignClass}`} style={style}>
            {children}
        </td>
    );
};

// WelcomeMessage component to display the username
interface WelcomeMessageProps {
    username: string;
}

const WelcomeMessage = ({ username }: WelcomeMessageProps) => (
    <p className="text-center text-lg text-gray-600 mb-6">
        Welcome, <span className="font-semibold">{username}</span>!
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
