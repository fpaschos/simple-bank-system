import React from 'react';
import './App.css';
import Global from './styles/Global';
import Dashboard from "./components/Dashboard";

const App: React.FC = () => {
    return (

        <>
            <Global/>
            <Dashboard/>
        </>

    );
}

export default App;
