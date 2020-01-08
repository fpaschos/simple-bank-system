import React from 'react';
import {FunctionComponent} from 'react';
import useAccountByIdService from "../services/AccountByIdService";

export interface Props {
    accountId?: string;
}

const AccountDetails: FunctionComponent<Props> = (props: Props) => {
    const balance = useAccountByIdService(props.accountId, 2000);
    return (
        <>
            {props.accountId && (
                <>
                    <p>Account: {balance.accountId}</p>
                    <p>Balance: {balance.balance}</p>
                </>
            )}

            {!props.accountId &&
                <p> Please select an account! </p>
            }

        </>
    );
}

export default AccountDetails;
