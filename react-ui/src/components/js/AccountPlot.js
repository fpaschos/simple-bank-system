import {LineSeries, MarkSeries, XAxis, XYPlot, YAxis} from 'react-vis';

import 'react-vis/dist/style.css';

import React, {useEffect, useRef, useState} from 'react';
import {useWindowSize} from "../../services/hooks";

// Using plain javascript for visual-vis components
const AccountPlot = (props) => {
    const {series} = props;
    const [width, setWidth] = useState(0);
    const [height, setHeight] = useState(0);
    const [highlightedX, setHighlightedX] = useState(null);

    const minValue = Math.min(...series.map(d => d.y));
    const maxValue = Math.max(...series.map(d => d.y));

    const yDomain = [0.98 * minValue, 1.02 * maxValue];

    const ref = useRef(null);

    const size = useWindowSize();

    // responsive width and height
    useEffect(() => {
        setWidth(ref.current.clientWidth);
        setHeight(ref.current.clientHeight);
    }, [size]);

    const onNearestX = (value, {index}) =>{
        value.i = index;
        setHighlightedX(value);
    };

    return (

        <div style={{width: '100%', height: '100%'}}
             ref={ref}
        >
            <div>Total points: {series.length}</div>
            {/*<div>{JSON.stringify(highlightedX)}</div>*/}

            <XYPlot
                width={width}
                height={height}
                xType="time"
                onMouseLeave={() => setHighlightedX(null)}
                yDomain={yDomain}
            >
                <XAxis/>
                <YAxis/>
                <LineSeries
                    onNearestX={onNearestX}
                    data={series}
                />

                {highlightedX ?
                    <LineSeries
                        data={[
                            {x: highlightedX && highlightedX.x, y: yDomain[0]},
                            {x: highlightedX && highlightedX.x, y: yDomain[1]}
                        ]}
                        stroke='rgba(17,147,154,0.7)'
                        strokeStyle='dashed'
                        strokeWidth={1}
                    /> : null
                }
                {highlightedX ?
                    <MarkSeries
                        data={[{
                            x: highlightedX && highlightedX.x,
                            y: highlightedX && series[highlightedX.i].y
                        }]}
                        color='rgba(17,147,154,0.7)'
                    /> : null
                }
            </XYPlot>
        </div>
    );
};

export default AccountPlot;
