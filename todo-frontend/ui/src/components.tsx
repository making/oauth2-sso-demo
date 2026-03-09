import { ReactNode, CSSProperties, ChangeEvent, ButtonHTMLAttributes } from 'react';

// Container for the whole TodoList app
interface ContainerProps {
    children: ReactNode;
}

const Container = ({ children }: ContainerProps) => (
    <div className="max-w-4xl mx-auto px-4 py-8 relative z-1">
        {children}
    </div>
);

// Header component for title
interface HeaderProps {
    children: ReactNode;
}

const Header = ({ children }: HeaderProps) => (
    <h1 className="text-5xl font-bold italic text-[#f0ead6] mb-3 tracking-tight" style={{ fontFamily: 'Georgia, "Times New Roman", Times, serif' }}>
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
        primary: 'text-[rgba(255,255,255,0.5)] hover:text-[#d4e157]',
        success: 'text-[#d4e157] hover:text-[#c6d343]',
        danger: 'text-[rgba(255,255,255,0.35)] hover:text-[#f87171]',
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
        className="w-full md:w-96 h-14 px-5 text-[1.0625rem] text-[#e0e0e0] bg-[rgba(255,255,255,0.04)] border border-[rgba(255,255,255,0.1)] rounded-[10px] transition-all duration-150 placeholder:text-[rgba(255,255,255,0.2)] hover:border-[rgba(255,255,255,0.18)] focus:outline-none focus:border-[rgba(255,255,255,0.3)] focus:bg-[rgba(255,255,255,0.06)]"
    />
);

// Table container with header and body styling
interface StyledTableProps {
    children: ReactNode;
}

const StyledTable = ({ children }: StyledTableProps) => (
    <div className="w-full overflow-x-auto rounded-[10px] border border-[rgba(255,255,255,0.1)] animate-fade-in">
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
    const baseClass = "p-3 text-sm border-b border-[rgba(255,255,255,0.08)] whitespace-nowrap overflow-hidden text-ellipsis";
    const alignClass = center ? "text-center" : "text-left";
    const headerClass = header ? "bg-[rgba(255,255,255,0.04)] font-medium text-[rgba(255,255,255,0.35)] text-xs uppercase tracking-[0.18em]" : "text-[#e0e0e0]";

    const style: CSSProperties = {};
    if (width) {
        style.width = width;
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
    <p className="text-[0.8125rem] font-medium text-[rgba(255,255,255,0.35)] tracking-[0.2em] uppercase">
        Welcome, <span className="text-[#f0ead6] font-semibold normal-case tracking-normal">{username}</span>!
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
