import { useState, useEffect, FormEvent } from 'react';
import { FaCheck, FaTrashAlt, FaPlus, FaSpinner } from 'react-icons/fa';
import useSWR, { useSWRConfig } from 'swr';
import {
    Container,
    Header,
    WelcomeMessage,
    StyledInput,
    StyledButton,
    StyledTable,
    TableCell,
    IconButton,
} from './components';

interface Todo {
    todoId: number;
    todoTitle: string;
    createdAt: string;
    createdBy: string;
    updatedAt: string;
    updatedBy: string;
    finished: boolean;
}

interface UserData {
    name: string;
    csrfToken: string;
}

// Custom fetcher function with error handling
const fetcher = async (url: string) => {
    const response = await fetch(url);

    if (!response.ok) {
        const error = new Error('An error occurred while fetching the data.');
        error.message = `Failed to fetch: ${response.status} ${response.statusText}`;
        throw error;
    }

    return response.json();
};

const TodoList = () => {
    const [newTodoTitle, setNewTodoTitle] = useState<string>('');
    const [hideCompleted, setHideCompleted] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState<boolean>(false);
    const { mutate } = useSWRConfig();

    // Using SWR to fetch todos
    const { data: todos, error: todosError, isLoading: isTodosLoading } = useSWR<Todo[]>('/api/todos', fetcher, {
        revalidateOnFocus: true,
        onError: (err) => {
            console.error('Error fetching todos:', err);
            setError('Failed to load todos. Please refresh the page.');
        }
    });

    // Using SWR to fetch user data
    const { data: userData, error: userError, isLoading: isUserLoading } = useSWR<UserData>('/whoami', fetcher, {
        revalidateOnFocus: false,
        onError: (err) => {
            console.error('Error fetching user data:', err);
            setError('Failed to load user data. Please refresh the page.');
        }
    });

    // Combined loading state
    const isLoading = isTodosLoading || isUserLoading;

    // Make sure we show error from any source (local state or SWR errors)
    // We use a side effect to set the local error state from SWR errors
    useEffect(() => {
        if (todosError || userError) {
            setError('Failed to load data. Please refresh the page.');
        }
    }, [todosError, userError]);

    const handleAddTodo = async (event: FormEvent) => {
        event.preventDefault();
        if (!newTodoTitle || submitting || !userData) return;

        setSubmitting(true);
        setError(null);

        try {
            const response = await fetch('/api/todos', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': userData.csrfToken,
                },
                body: JSON.stringify({ todoTitle: newTodoTitle }),
            });

            if (!response.ok) {
                throw new Error('Failed to create todo');
            }

            // Revalidate todos data after successful addition
            await mutate('/api/todos');
            setNewTodoTitle('');
        } catch (error) {
            console.error('Error creating todo:', error);
            setError('Failed to create todo. Please try again.');
        } finally {
            setSubmitting(false);
        }
    };

    const handleDeleteTodo = async (todoId: number) => {
        if (!userData) return;
        setError(null);

        try {
            const response = await fetch(`/api/todos/${todoId}`, {
                method: 'DELETE',
                headers: {
                    'X-CSRF-TOKEN': userData.csrfToken,
                },
            });

            if (!response.ok) {
                throw new Error('Failed to delete todo');
            }

            // Revalidate todos data after successful deletion
            await mutate('/api/todos');
        } catch (error) {
            console.error('Error deleting todo:', error);
            setError('Failed to delete todo. Please try again.');
        }
    };

    const handleToggleFinished = async (todoId: number) => {
        if (!todos || !userData) return;

        const todo = todos.find(todo => todo.todoId === todoId);
        if (!todo) return;

        setError(null);

        try {
            const response = await fetch(`/api/todos/${todoId}`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': userData.csrfToken,
                },
                body: JSON.stringify({ todoTitle: todo.todoTitle, finished: !todo.finished }),
            });

            if (!response.ok) {
                throw new Error('Failed to update todo');
            }

            // Revalidate todos data after successful update
            await mutate('/api/todos');
        } catch (error) {
            console.error('Error updating todo:', error);
            setError('Failed to update todo. Please try again.');
        }
    };

    // Format date to a more compact format to prevent line wrapping in table cells
    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        return new Intl.DateTimeFormat('default', {
            year: '2-digit',
            month: 'numeric',
            day: 'numeric',
            hour: 'numeric',
            minute: '2-digit'
        }).format(date);
    };

    if (isLoading) {
        return (
            <Container>
                <div className="flex flex-col items-center justify-center min-h-[50vh]">
                    <FaSpinner className="animate-spin text-4xl text-[#d4e157] mb-4" />
                    <p className="text-[rgba(255,255,255,0.35)]">Loading todos...</p>
                </div>
            </Container>
        );
    }

    return (
        <Container>
            <Header>Todo List</Header>

            {userData && (
                <div className="flex justify-between items-center mb-8">
                    <WelcomeMessage username={userData.name} />
                    <form action="/logout" method="post">
                        <input type="hidden" name="_csrf" value={userData.csrfToken} />
                        <StyledButton type="submit" variant="danger">Sign Out</StyledButton>
                    </form>
                </div>
            )}

            <div className="mb-8 bg-[rgba(255,255,255,0.04)] border border-[rgba(255,255,255,0.1)] p-6 rounded-[10px]">
                <form
                    onSubmit={handleAddTodo}
                    className="flex flex-col sm:flex-row items-center gap-3"
                >
                    <div className="w-full">
                        <StyledInput
                            value={newTodoTitle}
                            onChange={(e) => setNewTodoTitle(e.target.value)}
                            placeholder="What needs to be done?"
                        />
                    </div>
                    <StyledButton
                        type="submit"
                        disabled={submitting}
                        className="whitespace-nowrap flex items-center gap-2 h-14"
                    >
                        {submitting ? (
                            <>
                                <FaSpinner className="animate-spin" />
                                Adding...
                            </>
                        ) : (
                            <>
                                <FaPlus />
                                Add Task
                            </>
                        )}
                    </StyledButton>
                </form>
            </div>

            <div className="mb-4 flex justify-between items-center">
                <div className="flex items-center">
                    <label className="inline-flex items-center cursor-pointer">
                        <input
                            type="checkbox"
                            checked={hideCompleted}
                            onChange={() => setHideCompleted(!hideCompleted)}
                            className="sr-only peer"
                        />
                        <div className="relative w-11 h-6 bg-[rgba(255,255,255,0.1)] peer-focus:outline-hidden peer-focus:ring-2 peer-focus:ring-[#d4e157] rounded-full peer peer-checked:after:translate-x-full peer-checked:rtl:after:-translate-x-full peer-checked:after:border-[rgba(255,255,255,0.1)] after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-[rgba(255,255,255,0.3)] after:border-[rgba(255,255,255,0.1)] after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[#d4e157]"></div>
                        <span className="ms-3 text-sm font-medium text-[rgba(255,255,255,0.35)]">
                            Hide completed
                        </span>
                    </label>
                </div>
                <div className="text-sm text-[rgba(255,255,255,0.35)]">
                    {todos?.length || 0} {todos?.length === 1 ? 'task' : 'tasks'} total
                </div>
            </div>

            {error && (
                <div className="flex items-center gap-2 bg-[rgba(239,68,68,0.1)] border border-[rgba(239,68,68,0.25)] rounded-[8px] p-3 mb-4 animate-fade-in">
                    <svg className="w-4 h-4 text-[#f87171] shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10"/>
                        <line x1="15" y1="9" x2="9" y2="15"/>
                        <line x1="9" y1="9" x2="15" y2="15"/>
                    </svg>
                    <span className="text-[0.8125rem] text-[#fca5a5]">{error}</span>
                </div>
            )}

            {!todos || todos.length === 0 ? (
                <div className="bg-[rgba(255,255,255,0.04)] border border-[rgba(255,255,255,0.1)] p-8 text-center rounded-[10px] animate-fade-in">
                    <p className="text-[rgba(255,255,255,0.35)] mb-4">You don't have any tasks yet.</p>
                    <p className="text-[rgba(255,255,255,0.2)] text-sm">Add a new task to get started!</p>
                </div>
            ) : (
                <StyledTable>
                    <thead>
                        <tr>
                            <TableCell header width="30%">
                                Title
                            </TableCell>
                            <TableCell header width="15%">Created</TableCell>
                            <TableCell header width="15%">Created By</TableCell>
                            <TableCell header width="15%">Updated</TableCell>
                            <TableCell header width="15%">Updated By</TableCell>
                            <TableCell header width="10%" center>
                                Actions
                            </TableCell>
                        </tr>
                    </thead>
                    <tbody>
                        {todos
                            .filter(todo => !hideCompleted || !todo.finished)
                            .map(todo => (
                                <tr
                                    key={todo.todoId}
                                    className={`todo-row ${todo.finished ? 'todo-row-completed' : ''}`}
                                >
                                    <TableCell width="30%">
                                        <div className="flex items-center gap-2">
                                            {todo.finished && (
                                                <span className="inline-flex min-w-5 w-5 h-5 bg-[#d4e157] rounded-full items-center justify-center text-[#0e0e10]">
                                                    <FaCheck size={10} />
                                                </span>
                                            )}
                                            <span className={todo.finished ? 'line-through text-[rgba(255,255,255,0.35)]' : ''}>
                                                {todo.todoTitle}
                                            </span>
                                        </div>
                                    </TableCell>
                                    <TableCell width="15%">{formatDate(todo.createdAt)}</TableCell>
                                    <TableCell width="15%">{todo.createdBy}</TableCell>
                                    <TableCell width="15%">{formatDate(todo.updatedAt)}</TableCell>
                                    <TableCell width="15%">{todo.updatedBy}</TableCell>
                                    <TableCell width="10%" center>
                                        <div className="flex justify-center space-x-2">
                                            <IconButton
                                                onClick={() => handleToggleFinished(todo.todoId)}
                                                icon={<FaCheck />}
                                                variant={todo.finished ? 'success' : 'primary'}
                                                title={todo.finished ? 'Mark as incomplete' : 'Mark as complete'}
                                            />
                                            <IconButton
                                                onClick={() => handleDeleteTodo(todo.todoId)}
                                                icon={<FaTrashAlt />}
                                                variant="danger"
                                                title="Delete"
                                            />
                                        </div>
                                    </TableCell>
                                </tr>
                            ))}
                    </tbody>
                </StyledTable>
            )}
        </Container>
    );
};

export default TodoList;
