import React, {useEffect, useState} from 'react';
import {Panel} from "pivotal-ui/react/panels";
import {Table, Tbody, Td, Th, Thead, Tr} from 'pivotal-ui/react/table';
import {Icon} from 'pivotal-ui/react/iconography';
import {Form} from 'pivotal-ui/react/forms';
import {FlexCol, Grid} from 'pivotal-ui/react/flex-grids';
import {DefaultButton, PrimaryButton} from 'pivotal-ui/react/buttons';

import './App.css';
import 'pivotal-ui/css/typography';

function fetchMe() {
    return fetch('/me')
        .then(res => res.json());
}

function fetchTodos() {
    return fetch('/todos')
        .then(res => res.json());
}

function App() {
    const [me, setMe] = useState([]);
    const [todos, setTodos] = useState([]);

    useEffect(() => {
        fetchMe().then(setMe);
    }, []);
    useEffect(() => {
        fetchTodos().then(setTodos);
    }, []);

    const create = ({todoTitle}) => {
        fetch(`/todos`, {
            method: 'POST',
            body: JSON.stringify({todoTitle}),
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(fetchTodos)
            .then(setTodos);
    };
    const finish = (todoId) => {
        fetch(`/todos/${todoId}`, {
            method: 'PUT',
            body: JSON.stringify({finished: true}),
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(fetchTodos)
            .then(setTodos);
    };
    const remove = (todoId) => {
        fetch(`/todos/${todoId}`, {
            method: 'DELETE'
        })
            .then(fetchTodos)
            .then(setTodos);
    };
    return (
        <Panel>
            <h1>Todo List</h1>
            <p>Welcome, {me.name}!</p>
            <div>
                <Form {...{
                    onSubmit: ({current}) => create(current),
                    fields: {
                        todoTitle: {
                            inline: true,
                            label: 'Todo Title',
                            initialValue: ''
                        }
                    }
                }}>
                    {({fields, canSubmit}) => {
                        return (
                            <Grid>
                                <FlexCol>{fields.todoTitle}</FlexCol>
                                <FlexCol>
                                    <DefaultButton type="submit"
                                                   disabled={!canSubmit()}>Add
                                    </DefaultButton>
                                </FlexCol>
                            </Grid>
                        );
                    }}
                </Form>
            </div>
            <Table className="pui-table--tr-hover">
                <Thead>
                    <Tr>
                        <Th>Title</Th><Th>Created At</Th><Th>Created By</Th><Th>Updated At</Th><Th>Updated
                        By</Th><Th>Actions</Th>
                    </Tr>
                </Thead>
                <Tbody>
                    {
                        todos.map(todo =>
                            <Tr key={todo.todoId}>
                                <Td>{todo.finished ? <React.Fragment>
                                    <Icon src="check" style={{fontSize: '20px', fill: 'green'}}/>&nbsp;
                                </React.Fragment> : ``}{todo.todoTitle}</Td>
                                <Td>{todo.createdAt}</Td>
                                <Td>{todo.createdBy}</Td>
                                <Td>{todo.updatedAt}</Td>
                                <Td>{todo.updatedBy}</Td>
                                <Td>
                                    {!todo.finished &&
                                    <React.Fragment>
                                        <Icon src="check" style={{fontSize: '20px'}}
                                              onClick={() => finish(todo.todoId)}/>
                                        &nbsp;
                                    </React.Fragment>
                                    }
                                    <Icon src="trash" style={{fontSize: '20px'}}
                                          onClick={() => remove(todo.todoId)}/>
                                </Td>
                            </Tr>)
                    }
                </Tbody>
            </Table>
            <form action={'/logout'} method={'POST'}>
                <PrimaryButton type="submit">Logout</PrimaryButton>
            </form>
        </Panel>
    );
}

export default App;
