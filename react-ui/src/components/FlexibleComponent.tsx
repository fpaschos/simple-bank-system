import React, {FunctionComponent, ReactElement, ReactNode, useEffect, useRef, useState} from "react";
import {useWindowSize} from "../services/hooks";

export interface Props {
    children: {
        content: ReactNode
    }
}

const FlexibleComponent: FunctionComponent<Props> = (props: Props) => {

    const [width, setWidth] = useState(0);
    const [height, setHeight] = useState(0);

    const ref = useRef<HTMLDivElement>(null);
    const size = useWindowSize();

    // responsive width and height
    useEffect(() => {
        if(ref.current !== null ) {
            setWidth(ref.current.clientWidth);
            setHeight(ref.current.clientHeight);
        }
    }, [size]);

    return (
        <div ref={ref} style={{width: '100%', height: '100%'}}>
            {React.cloneElement(props.children.content as React.ReactElement<any>, {
                width: width,
                height: height
            })}
        </div>);
};

export default FlexibleComponent;

