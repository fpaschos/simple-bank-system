import styled from 'styled-components';
import px2vw from "./utils";
import chroma from 'chroma-js';

export const BoxContainer = styled.div`
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
  height: 50vh;
  width:  ${(props)=> props.w}%;
`;

export const selectDarkTheme = (theme:any) => {
   return ({
        ...theme,
        colors: {
            ...theme.colors,
            primary: '#333',
            primary25: chroma('#333').brighten(0.99).css(),
        }
    })
};

export const selectDarkStyles = {
    menu: (provided: any, state: any) => ({
        ...provided,
       backgroundColor: '#111'
    })
}

