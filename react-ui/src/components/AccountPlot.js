import {
    XYPlot,
    XAxis,
    YAxis,
    VerticalGridLines,
    HorizontalGridLines,
    LineMarkSeriesCanvas,
    LineMarkSeries,
    LineSeriesCanvas,
    LineSeries,
    Crosshair
} from 'react-vis';

import 'react-vis/dist/style.css';

import React from 'react';

const AccountPlot = (props) => {

    const d = props.data;

    return (
        // <p>{JSON.stringify(data)}</p>
        <XYPlot
            width={300}
            height={300}
            xType="time"
            // style={{ fill: 'none'}}

        >
            <VerticalGridLines />
            <HorizontalGridLines />
            <XAxis />
            <YAxis />
            <LineSeries
                data={d}/>
        </XYPlot>
    );
};

export default AccountPlot;