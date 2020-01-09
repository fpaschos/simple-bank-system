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

import React, {useEffect, useRef, useState} from 'react';
import {useWindowSize} from "../services/hooks";

const AccountPlot = (props) => {

    const {data} = props;
    const [width, setWidth] = useState(0);
    const [height, setHeight] = useState(0);
    const ref = useRef(null);

    const size = useWindowSize();

    // responsive width and height
    useEffect(() => {
        setWidth(ref.current.clientWidth);
        // setHeight(ref.current.clientHeight > 400 ? ref.current.clientHeight : 400);
        setHeight(400)
    }, [size]);


    return (
        <div style={{width: '100%', height: '100%'}} ref={ref}>
            <XYPlot
                width={width}
                height={height}
                xType="time"
                // style={{ fill: 'none'}}

            >
                <VerticalGridLines/>
                <HorizontalGridLines/>
                <XAxis/>
                <YAxis/>
                <LineSeries
                    data={data}/>
            </XYPlot>
        </div>
    );
};

export default AccountPlot;