import {useState, useEffect, FormEvent} from 'react';
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

function TodoList() {
    const [todos, setTodos] = useState<Todo[]>([]);
    const [username, setUsername] = useState<string>('');
    const [csrfToken, setCsrfToken] = useState<string>('');
    const [newTodoTitle, setNewTodoTitle] = useState<string>('');
    const [hideCompleted, setHideCompleted] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchTodos = async () => {
            try {
                const response = await fetch('/api/todos');
                if (!response.ok) {
                    throw new Error('Failed to fetch todos');
                }
                const data: Todo[] = await response.json();
                setTodos(data);
            } catch (error) {
                console.error('Error fetching todos:', error);
                setError('Failed to fetch todos. Please try again.');
            }
        };

        const fetchUsername = async () => {
            try {
                const response = await fetch('/whoami');
                if (!response.ok) {
                    throw new Error('Failed to fetch username');
                }
                const data = await response.json();
                setUsername(data.name);
                setCsrfToken(data.csrfToken);
            } catch (error) {
                console.error('Error fetching username:', error);
                setError('Failed to fetch username. Please try again.');
            }
        };

        fetchTodos();
        fetchUsername();
    }, []);

    const handleAddTodo = async (event: FormEvent) => {
        event.preventDefault();
        if (!newTodoTitle) return;

        setError(null);

        try {
            const response = await fetch('/api/todos', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken,
                },
                body: JSON.stringify({todoTitle: newTodoTitle}),
            });

            if (!response.ok) {
                throw new Error('Failed to create todo');
            }

            const newTodo: Todo = await response.json();
            setTodos([...todos, newTodo]);
            setNewTodoTitle('');
        } catch (error) {
            console.error('Error creating todo:', error);
            setError('Failed to create todo. Please try again.');
        }
    };

    const handleDeleteTodo = async (todoId: number) => {
        setError(null);

        try {
            const response = await fetch(`/api/todos/${todoId}`, {
                method: 'DELETE',
                headers: {
                    'X-CSRF-TOKEN': csrfToken,
                },
            });

            if (!response.ok) {
                throw new Error('Failed to delete todo');
            }

            setTodos(todos.filter((todo) => todo.todoId !== todoId));
        } catch (error) {
            console.error('Error deleting todo:', error);
            setError('Failed to delete todo. Please try again.');
        }
    };

    const handleToggleFinished = async (todoId: number) => {
        const todo = todos.find((todo) => todo.todoId === todoId);
        if (!todo) return;

        setError(null);

        try {
            const response = await fetch(`/api/todos/${todoId}`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken,
                },
                body: JSON.stringify({finished: !todo.finished}),
            });

            if (!response.ok) {
                throw new Error('Failed to update todo');
            }

            const updatedTodo: Todo = await response.json();
            setTodos(todos.map((t) => (t.todoId === todoId ? updatedTodo : t)));
        } catch (error) {
            console.error('Error updating todo:', error);
            setError('Failed to update todo. Please try again.');
        }
    };

    return (
        <Container>
            <Header>Todo List</Header>
            {username && <WelcomeMessage username={username}/>}
            <form
                onSubmit={handleAddTodo}
                style={{display: 'flex', justifyContent: 'center', marginBottom: '20px'}}
            >
                <StyledInput
                    value={newTodoTitle}
                    onChange={(e) => setNewTodoTitle(e.target.value)}
                    placeholder="Enter todo title"
                />
                <StyledButton type="submit">Add</StyledButton>
            </form>
            <div style={{textAlign: 'center', marginBottom: '10px'}}>
                <label>
                    <input
                        type="checkbox"
                        checked={hideCompleted}
                        onChange={() => setHideCompleted(!hideCompleted)}
                        style={{marginRight: '5px'}}
                    />
                    Hide completed todos
                </label>
            </div>
            {error && <p style={{color: 'red', textAlign: 'center'}}>{error}</p>}
            <StyledTable>
                <thead>
                <tr>
                    <TableCell header width={'200px'}>
                        Title
                    </TableCell>
                    <TableCell header>Created At</TableCell>
                    <TableCell header>Created By</TableCell>
                    <TableCell header>Updated At</TableCell>
                    <TableCell header>Updated By</TableCell>
                    <TableCell header center>
                        Actions
                    </TableCell>
                </tr>
                </thead>
                <tbody>
                {todos
                    .filter((todo) => !hideCompleted || !todo.finished)
                    .map((todo) => (
                        <tr
                            key={todo.todoId}
                            style={{backgroundColor: todo.finished ? '#eaffea' : '#fff'}}
                        >
                            <TableCell width="200px">
                                {todo.finished ? <span>&#10004;</span> : ''} {todo.todoTitle}
                            </TableCell>
                            <TableCell>{new Date(todo.createdAt).toLocaleString()}</TableCell>
                            <TableCell>{todo.createdBy}</TableCell>
                            <TableCell>{new Date(todo.updatedAt).toLocaleString()}</TableCell>
                            <TableCell>{todo.updatedBy}</TableCell>
                            <TableCell center>
                                <IconButton
                                    onClick={() => handleToggleFinished(todo.todoId)}
                                    icon={todo.finished ? <span>&#10004;</span> : <span>&#10003;</span>}
                                    color={todo.finished ? '#28a745' : '#007bff'}
                                    title="Toggle Finished"
                                />
                                <IconButton
                                    onClick={() => handleDeleteTodo(todo.todoId)}
                                    icon="&#128465;"
                                    color="#dc3545"
                                    title="Delete"
                                />
                            </TableCell>
                        </tr>
                    ))}
                </tbody>
            </StyledTable>
        </Container>
    );
}

export default TodoList;
