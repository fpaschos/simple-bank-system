import {Borders, Highlight, Hint, LineSeries, MarkSeries, XAxis, XYPlot, YAxis} from 'react-vis';

import 'react-vis/dist/style.css';

import React, {useState} from 'react';


// Using plain javascript for visual-vis components
const AccountPlot = (props) => {
    const {series} = props;
    const [highlightedX, setHighlightedX] = useState(null);

    const [drawLocation, setDrawLocation] = useState(null);

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
            {/*<div>{JSON.stringify(drawLocation)}</div>*/}

            <XYPlot
                width={props.width}
                height={props.height}
                xType="time"
                onMouseLeave={() => setHighlightedX(null)}
                yDomain={yDomain}
                xDomain={drawLocation && [drawLocation.left, drawLocation.right]}
            >
                <LineSeries
                    onNearestX={onNearestX}
                    data={series}
                />
                <Borders style={{
                    bottom: {fill: '#282B30'},
                    left: {fill: '#282B30'},
                    right: {fill: '#282B30'},
                    top: {fill: '#282B30'}
                }}/>
                <XAxis/>
                <YAxis/>


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
                {highlightedX ?
                    <Hint value={{x: highlightedX.x, y: yDomain[1]}}>
                        <div>
                            <div>Balance:</div>
                            <div>{highlightedX.y} &euro;</div>
                        </div>
                    </Hint> : null
                }
                <Highlight
                    enableY={false}
                    onBrushEnd={area => setDrawLocation(area)}
                    onDrag = { area => setDrawLocation( old  => {
                        return {
                            left: old.left - (area.right - area.left),
                            right: old.right - (area.right - area.left)
                        }
                    })}
                />

            </XYPlot>
        </>
    );
};

export default AccountPlot;
