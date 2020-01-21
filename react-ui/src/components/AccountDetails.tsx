import React, {FunctionComponent} from 'react';
import useAccountByIdService from "../services/AccountByIdService";
import moment from 'moment';
import AccountDetailsGraph from "./AccountDetailsGraph";

const AccountDetails: FunctionComponent<Props> = (props: Props) => {
    const balance = useAccountByIdService(props.accountId, 2000);
    return (
        <>
            {props.accountId && (
                <>
                    <h3>Account: {balance.accountId}</h3>
                    <h5>Balance: {balance.balance} &euro; </h5>
                    <h5>Last Update: {moment(balance.updated).format("DD/MM/YYYY hh:mm a")} </h5>
                    <AccountDetailsGraph accountId={props.accountId}/>
                </>
            )}

            {!props.accountId &&
            <p> Please select an account! </p>
            }
        </>
    );
};

export interface Props {
    accountId?: string;
}

export default AccountDetails;
