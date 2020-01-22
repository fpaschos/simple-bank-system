import React, {FunctionComponent} from 'react';
import useAccountByIdService from "../services/AccountByIdService";
import moment from 'moment';
import AccountDetailsGraph from "./AccountDetailsGraph";
import {Message, Statistic} from 'semantic-ui-react';

const AccountDetails: FunctionComponent<Props> = (props: Props) => {
    const balance = useAccountByIdService(props.accountId, 2000);
    return (
        <>
            {props.accountId && (
                <>
                    <Statistic.Group size='mini' widths='three'>
                        <Statistic color='purple'>
                            <Statistic.Value>{balance.accountId}</Statistic.Value>
                            <Statistic.Label>Account</Statistic.Label>
                        </Statistic>
                        <Statistic color='teal'>
                            <Statistic.Value>{balance.balance}  &euro;</Statistic.Value>
                            <Statistic.Label>Balance</Statistic.Label>
                        </Statistic>
                        <Statistic color='grey'>
                            <Statistic.Value>{moment(balance.updated).format("DD/MM/YYYY hh:mm:ss a")}</Statistic.Value>
                            <Statistic.Label>Updated</Statistic.Label>
                        </Statistic>

                    </Statistic.Group>
                    <AccountDetailsGraph accountId={props.accountId}/>
                </>
            )}
            {!props.accountId && <Message> Please select an account! </Message>}
        </>
    );
};

export interface Props {
    accountId?: string;
}

export default AccountDetails;
