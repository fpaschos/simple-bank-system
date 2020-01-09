import styled from 'styled-components';
import px2vw from "./utils";

export const Container = styled.div`
      display: flex;
      flex-flow: row wrap;
      min-width: 100%;
      min-height: 100%;
`;

export interface BoxProps {
    w: number;
}

export const Box = styled.div<BoxProps>`
  padding: ${px2vw(20)};
  width:  ${(props)=> props.w}%;
`;