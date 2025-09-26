import mysql.connector
from typing import Optional
from model.params import TuningParams
from db.connection import get_conn

def insert_tuning_params(params: TuningParams):
    """
    Inserts a TuningParams instance into CricketMatchData.TunedParameters
    """
    sql = """
        INSERT INTO CricketMatchData.TunedParameters
        (e_value, k_factor, starting_elo, home_adv, toss_adv, win_margin, max_draw_chance)
        VALUES (%s, %s, %s, %s, %s, %s, %s)
    """
    data = (
        params.e_value,
        params.k_factor,
        params.starting_elo,
        params.home_adv,
        params.toss_adv,
        params.win_margin,      # can be None, maps to null
        params.max_draw_chance,
    )

    conn = get_conn()
    cursor = conn.cursor()
    cursor.execute(sql, data)
    conn.commit()
    tuning_id = cursor.lastrowid
    cursor.close()
    return tuning_id
