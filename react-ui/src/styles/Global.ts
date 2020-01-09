import {createGlobalStyle} from "styled-components";

const Global = createGlobalStyle`
    * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
      }
      
      body {
          display: flex;
          min-height: 100vh;
          flex-direction: column;
          margin: 0;
      }
        
     
      body > #root > div {
        height: 100vh;
      }
      
      
      :root {
        
      }
`;

export default Global