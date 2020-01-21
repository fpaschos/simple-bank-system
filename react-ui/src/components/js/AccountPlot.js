import {LineSeries, MarkSeries, XAxis, XYPlot, YAxis} from 'react-vis';

import 'react-vis/dist/style.css';

import React, {useEffect, useState} from 'react';

// Using plain javascript for visual-vis components
const AccountPlot = (props) => {
    const {series} = props;
    const [highlightedX, setHighlightedX] = useState(null);

    const minValue = Math.min(...series.map(d => d.y));
    const maxValue = Math.max(...series.map(d => d.y));

    const yDomain = [0.98 * minValue, 1.02 * maxValue];


    const onNearestX = (value, {index}) =>{
        value.i = index;
        setHighlightedX(value);
    };

    return (

        <>
            {/*<div>Total points: {series.length}</div>*/}
            {/*<div>{JSON.stringify(highlightedX)}</div>*/}

            <XYPlot
                width={props.width}
                height={props.height}
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
        </>
    );
};

export default AccountPlot;
